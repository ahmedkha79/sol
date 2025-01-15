package de.hamburg.sol.vs.server.messages.logic;


import de.hamburg.sol.vs.config.global.GlobalConfig;
import de.hamburg.sol.vs.galaxy.datatype.StarInfo;
import de.hamburg.sol.vs.galaxy.model.GalaxyModel;
import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.messages.datatype.Message;
import de.hamburg.sol.vs.messages.datatype.MessageList;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Log4j2
@Lazy
public class MessageService {

    private SolServer solServer;

    private GalaxyModel galaxyModel;

    private RestTemplate restTemplate;

    private String solStarUUID;


    private final Map<String, Message> messages = new ConcurrentHashMap<>();
    private final Map<String, Set<StarInfo>>  sentToStarMap = new ConcurrentHashMap<>();
    private final AtomicLong nonceCounter = new AtomicLong(1);

    private final List<String> validScopes = List.of("active", "all");

    private final String metaView = "id";
    private final String allView = "header";

    private final List<String> validViews = List.of(metaView, allView);

    private final String activeStatus = "active";
    private final String deleteStatus = "deleted by";



    public MessageService(@Lazy SolServer solServer, GalaxyModel galaxyModel, RestTemplate restTemplate) throws IllegalAccessException {
        this.solServer = solServer;
        this.galaxyModel = galaxyModel;
        this.solStarUUID = GlobalConfig.getStarUUID();
        this.restTemplate = restTemplate;
    }

    /**
     * Checks if the given Message has the correct star
     * @param star
     * @return true if star is correct, false if star is null, empty or not equal to the sol StarUUID
     */
    public boolean validateStar(String star){
        if(star == null || star.isEmpty() || !star.equals(solStarUUID)){
            return false;
        }

        return true;
    }


    /**
     * Validates the format of a Message
     * @param message
     * @return true if message has the subject and origin, false if they are null or empty
     */
    public boolean validateFormat(Message message){
        if(checkIfStringIsEmpty(message.getSubject()) || checkIfStringIsEmpty(message.getOrigin())){
            return false;
        }

        return true;
    }

    /**
     * Checks if the MSG_ID is empty
     * @param msg_id
     * @return true if the MSG_ID is empty, false if not
     */

    public boolean checkIfMsgIDIsEmpty(String msg_id){
        if(!checkIfStringIsEmpty(msg_id)){
            return false;
        }
        return true;
    }

    public boolean checkMessageWithID(String msg_id){
        return messages.containsKey(msg_id);
    }

    private boolean checkIfStringIsEmpty(String string){
        if(string == null || string.isEmpty()){
            return true;
        }
        return false;
    }

    public boolean checkTimeStamp(Message message){
        LocalDateTime created = message.getCreated();
        LocalDateTime changed = message.getChanged();

        if(changed.isBefore(created)){
            return false;
        }
        return true;
    }

    public String cutToNLRemoveCR(String subject){
        //Kürze bis zum ersten Newline
        String nw = subject.split("\n")[0];
        return nw.replace("\r", "");
    }

    public void setInitialVersion(Message message){
        message.setVersion(1);
    }

    public boolean checkVersion(Message message){
        if(message != null && message.getVersion() != 1){
            return false;
        }

        return true;
    }

    private String createMSGID(Message message){

        String msgID = String.format("%s.@.%s", nonceCounter.getAndIncrement(), message.getOrigin() );
        return msgID;
    }

    private void setTimeStamps(Message message){
        LocalDateTime created = LocalDateTime.now();
        message.setCreated(created);
        message.setChanged(created);
    }

    private void setStatus(Message message){
        setStatus(message, activeStatus );
    }

    public String processMessageAndForward(Message message){
        Message message1 = processMessage(message);
        forwardMessageToStar(message1);
        return message1.getMsg_id();

    }

    public void receiveMessageAndSave(Message message){
        if(!messages.containsKey(message.getMsg_id())) {
            message.updateReceived();
            message.setFrom_star(message.getStar());
            log.info("{}", message);
            messages.put(message.getMsg_id(), message);
        }

    }

    public void receiveDeleteAndForward(String msgUUID, String starUUID){
        String deleteStatusMsg;
        if(this.solStarUUID.equals(starUUID)){
           deleteStatusMsg = String.format("%s us from %s", deleteStatus, starUUID);
        } else {
            deleteStatusMsg = String.format("%s by %s", deleteStatus, starUUID);
        }
        Message message = getMessage(msgUUID);
        updateDeleteMessageAndSave(deleteStatusMsg, message, starUUID );

        if(sentToStarMap.containsKey(msgUUID)){
            forwardDeleteMessages(msgUUID, starUUID);
        }
    }

    private void updateDeleteMessageAndSave(String status, Message message, String starUUID){
        message.setStatus(status);
        message.updateChanged();
        message.updateRemoved();
        messages.put(message.getMsg_id(), message);

    }

    private void forwardDeleteMessages(String msg_id, String starUUID){
        log.info("Leite Delete-Request an alle Sterne weiter, zu dem Sol die Nachricht: {} geschickt hat", msg_id);
        sentToStarMap.get(msg_id).forEach(starInfo -> {
            forwardDelete(starInfo, msg_id, starUUID);
        });
    }

    private void forwardDelete(StarInfo starInfo, String msg_id, String starUUID){
        String url = String.format("http://%s:%d/vs/v2/messages/%s?star=%s", starInfo.getIpAddress(), starInfo.getPort(), msg_id, starUUID);
        restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    }


    public Message processMessage(Message message){
        message.setSubject(cutToNLRemoveCR(message.getSubject()));
        setInitialVersion(message);
        setTimeStamps(message);
        setStatus(message);
        String origin = createOrigin(message);
        message.setOrigin(origin);
        String msgID = createMSGID(message);
        message.setMsg_id(msgID);
        messages.put(msgID, message);

        log.debug("Nachricht wurde erfolgreich erzeugt");
        log.info("Nachricht: {} ", message);
        log.debug("Map-Size: {}", messages.size());

        return message;
    }

    private String createOrigin(Message message){
        String comUUID;
        if(checkIfStringIsEmpty(message.getOrigin())){
            comUUID = message.getSender();
        } else {
            comUUID = message.getOrigin();
        }
        String origin = String.format("%s.:.%s", comUUID, solStarUUID);
        return origin;
    }

    @Async
    public void forwardMessageToStar(Message message){
        Message toSent = naiveClone(message);

        log.info("Leite Message an alle bekannten Sterne weiter");
        galaxyModel.getAllStars().forEach(starInfo -> {


            log.info("Nachricht: {}", toSent);
            sendMessageToStar(toSent, starInfo);

            if(!starInfo.getStar().equals(solStarUUID)) {
                storeSentToStar(message.getMsg_id(), starInfo);
                saveSentInfo(message, starInfo);
            }

        });
        log.info("Nachricht nach Weiterleitung: {}", message);
        messages.put(message.getMsg_id(), message);
    }

    private void storeSentToStar(String msg_id, StarInfo starInfo){
        sentToStarMap.computeIfAbsent(msg_id, k -> new HashSet<>()).add(starInfo);
    }

    private void sendMessageToStar(Message message, StarInfo starInfo){
        String url = String.format("http://%s:%d/vs/v2/messages/%s", starInfo.getIpAddress(), starInfo.getPort(), message.getMsg_id());
        log.info("Leite Nachricht an Stern: {} weiter", starInfo.getStar());

        restTemplate.postForEntity(url, message, String.class);
    }

    private void saveSentInfo(Message message, StarInfo starInfo){
        message.putIntoDelivered(starInfo.getStar(), getLocalTime());
    }


    private Message naiveClone(Message message){
        return Message.builder().msg_id(message.getMsg_id())
                .origin(message.getOrigin())
                .sender(message.getSender())
                .created(message.getCreated())
                .changed(message.getChanged())
                .message(message.getMessage())
                .subject(message.getSubject())
                .status(message.getStatus())
                .version(message.getVersion())
                .star(message.getStar())
                .build();

    }





    public void setStatus(Message message, String status){
        message.setStatus(status);
    }

    public MessageList getMessages(String star, String scope, String view){
        String tempScope = validScopes.contains(scope) ? scope : activeStatus;

        String  tempView = validViews.contains(view) ? view : metaView;



        List<Message> messageListResponse = messages.values().stream()
                .filter(msg -> "all".equals(tempScope) || activeStatus.equals(msg.getStatus()))
                .filter(msg -> "all".equals(star) || star.equals(msg.getStar()))
                .map(msg -> mapToMessageView(msg, tempView))
                .toList();
        return createMessageListResponse(messageListResponse, tempScope, tempView);


    }


    public MessageList getSingleMessage(String msg_id){
        Message msg = messages.get(msg_id);
        Message result;
        if(msg == null){
            return null;
        }

        if(msg.getStatus().equals(activeStatus)){
            result = mapToMessageView(msg, allView);
        } else {
            result = mapToMessageView(msg, metaView);
        }

        return createMessageListResponse(List.of(result), null, null);
    }

    public Message getMessage(String msg_id){
        return messages.get(msg_id);
    }

    private MessageList createMessageListResponse(List<Message> messages, String scope, String view){
        MessageList messageList = new MessageList();
        messageList.setStar(solStarUUID);
        messageList.setTotalResults(messages.size());
        messageList.setScope(scope);
        messageList.setView(view);
        messageList.setMessages(messages);

        return messageList;
    }

    private Message mapToMessageView(Message msg, String view){
        if("header".equals(view)){
            return msg;
        } else {
            return shortMessage(msg);
        }
    }

    private Message shortMessage(Message message){

        Message sent = Message.builder()
                .msg_id(message.getMsg_id())
                .status(message.getStatus())
                .build();

        if(message.getDelivered() != null || !message.getDelivered().isEmpty()){
            sent.setDelivered(message.getDeliveredStars());
        } else {
            sent.setDelivered(List.of());
        }

        return sent;
    }

    public boolean containsStar(String starUUID){
        return galaxyModel.containsStar(starUUID);
    }

    private LocalDateTime getLocalTime(){
        return LocalDateTime.now();
    }

}
