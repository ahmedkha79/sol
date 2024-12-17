package de.hamburg.sol.vs.server.messages.logic;


import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.server.messages.datatype.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Log4j2
public class MessageService {

    private SolServer solServer;

    private final Map<String, Message> messages = new HashMap<>();
    private final AtomicLong nonceCounter = new AtomicLong(1);

    public MessageService(@Lazy SolServer solServer) {
        this.solServer = solServer;
    }

    /**
     * Checks if the given Message has the correct star
     * @param message
     * @return true if star is correct, false if star is null, empty or not equal to the sol StarUUID
     */
    public boolean validateStar(Message message){
        if(message.getStar() == null || message.getStar().isEmpty() || !message.getStar().equals(solServer.getStarUUID())){
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
     * @param message
     * @return true if the MSG_ID is empty, false if not
     */

    public boolean checkIfMsgIDIsEmpty(Message message){
        if(!checkIfStringIsEmpty(message.getMsg_id())){
            return false;
        }
        return true;
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
        message.setStatus("active");
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

}
