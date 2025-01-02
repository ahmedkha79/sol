package de.hamburg.sol.vs.server.messages.logic;


import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.server.messages.datatype.Message;
import de.hamburg.sol.vs.server.messages.datatype.MessageList;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Log4j2
public class MessageService {

    private SolServer solServer;

    private final Map<String, Message> messages = new HashMap<>();
    private final AtomicLong nonceCounter = new AtomicLong(1);

    private final List<String> validScopes = List.of("active", "all");

    private final String metaView = "id";
    private final String allView = "header";

    private final List<String> validViews = List.of(metaView, allView);

    private final String activeStatus = "active";
    private final String deleteStatus = "deleted";



    public MessageService(@Lazy SolServer solServer) {
        this.solServer = solServer;
    }

    /**
     * Checks if the given Message has the correct star
     * @param star
     * @return true if star is correct, false if star is null, empty or not equal to the sol StarUUID
     */
    public boolean validateStar(String star){
        if(star == null || star.isEmpty() || !star.equals(solServer.getStarUUID())){
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
        String comUUID;
        if(checkIfStringIsEmpty(message.getOrigin())){
           comUUID = message.getSender();
        } else {
            comUUID = message.getOrigin();
        }

        String msgID = String.format("%s.@.%s", nonceCounter.getAndIncrement(), comUUID);
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

    public Message processMessage(Message message){
        message.setSubject(cutToNLRemoveCR(message.getSubject()));
        setInitialVersion(message);
        setTimeStamps(message);
        setStatus(message);
        String msgID = createMSGID(message);
        message.setMsg_id(msgID);
        messages.put(msgID, message);

        log.debug("Nachricht wurde erfolgreich erzeugt");
        log.debug("Map-Size: {}", messages.size());

        return message;
    }

    public boolean deleteMessage(String msgID){
        Message message = messages.get(msgID);
        if(message == null){
            return false;
        } else {
            setStatus(message, deleteStatus);
            message.updateChanged();
        }
        return true;
    }

    public void setStatus(Message message, String status){
        message.setStatus(status);
    }

    public MessageList getMessages(String starUUID, String scope, String view){
        String tempScope = validScopes.contains(scope) ? scope : activeStatus;

        String  tempView = validViews.contains(view) ? view : metaView;


        List<Message> messageListResponse = messages.values().stream()
                .filter(msg -> "all".equals(tempScope) || activeStatus.equals(msg.getStatus()))
                .filter(msg -> starUUID.equals(msg.getStar()))
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

        if(msg.getStatus() == activeStatus){
            result = mapToMessageView(msg, allView);
        } else {
            result = mapToMessageView(msg, metaView);
        }

        return createMessageListResponse(List.of(result), null, null);
    }

    private MessageList createMessageListResponse(List<Message> messages, String scope, String view){
        MessageList messageList = new MessageList();
        messageList.setStar(solServer.getStarUUID());
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
            return Message.builder()
                    .msg_id(msg.getMsg_id())
                    .status(msg.getStatus())
                    .build();
        }
    }

}
