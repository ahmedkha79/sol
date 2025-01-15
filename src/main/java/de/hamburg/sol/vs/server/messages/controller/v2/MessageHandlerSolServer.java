package de.hamburg.sol.vs.server.messages.controller.v2;

import de.hamburg.sol.vs.messages.api.MessageHandler;
import de.hamburg.sol.vs.messages.datatype.Message;
import de.hamburg.sol.vs.messages.datatype.MessageList;
import de.hamburg.sol.vs.server.messages.logic.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@Lazy
public class MessageHandlerSolServer implements MessageHandler {

    private final MessageService messageService;

    public MessageHandlerSolServer(@Lazy final MessageService messageService) {
        this.messageService = messageService;
    }


    public ResponseEntity<String> handlePostMessageRequest(Message message) {
        if(!messageService.validateStar(message.getStar())){
            log.info("star: {} ist inkorrekt oder nicht angegeben", message.getStar());
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!messageService.validateFormat(message)){
            log.info("Format der Nachricht ist inkorrekt, überprüfe Subject: {} und Origin: {}", message.getSubject(), message.getOrigin());
            return ResponseEntity.status(412).body(HttpStatus.PRECONDITION_FAILED.getReasonPhrase());
        }

        if(!messageService.checkIfMsgIDIsEmpty(message.getMsg_id())){
            log.info("MSG_ID: {} ist nicht leer sein", message.getMsg_id());
            return ResponseEntity.status(412).body(HttpStatus.PRECONDITION_FAILED.getReasonPhrase());
        }


        if(!messageService.checkVersion(message)){
            log.info("Ungültige Version: {} angegeben", message.getVersion());
            return ResponseEntity.status(412).body(HttpStatus.PRECONDITION_FAILED.getReasonPhrase());
        }


        String msgUUID = messageService.processMessageAndForward(message);
        log.info("Nachricht wurde von Sol aufgenommen und gespeichert");

        return ResponseEntity.status(200).body(msgUUID);


    }

    @Override
    public ResponseEntity<String> handleReceivedPostMessageRequest(String msg_id, Message message) {

        if(msg_id == null || msg_id.isEmpty()){
            log.info("Msg_id: {} ist leer oder null", msg_id);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        messageService.receiveMessageAndSave(message);
        log.info("Nachricht von Stern: {} erhalten", message.getFrom_star());
        log.debug("Folgende Nachricht: {} erhalten", message);

        return ResponseEntity.ok("Message received and saved");
    }

    public ResponseEntity<String> handleDeleteMessage( String msg_id, String star){
      if(!messageService.containsStar(star)){
          log.info("Stern: {} in der Galaxie nicht bekannt", star);
          return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
      }

      if(messageService.checkIfMsgIDIsEmpty(msg_id) || !messageService.checkMessageWithID(msg_id)){
          log.info("Msg_id: {} ist leer oder konnte nicht gefunden werden", msg_id);
          return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
      }

      messageService.receiveDeleteAndForward(msg_id, star);
      log.info("Nachricht: {} wird gelöscht", msg_id);
      return ResponseEntity.ok("Message deleted");



    }


    public ResponseEntity<?> handleGetMessageRequest(String star, String scope, String view){

        if(!star.equals("all") && !messageService.containsStar(star)){
            log.info("star: {} ist inkorrekt oder nicht angegeben", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        log.info("scope: {} und view: {}", scope,view);

        MessageList messageList = messageService.getMessages(star, scope, view);

        return ResponseEntity.status(200).body(messageList);


    }

    public ResponseEntity<?> handleGetSingleMessage(String msg_id, String star){
        if(!messageService.validateStar(star)){
            log.info("star :{} ist inkorrekt oder nicht angegeben", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!messageService.checkMessageWithID(msg_id)){
            log.info("Nachricht mit ID: {} existiert nicht", msg_id);
            return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
        }

        log.info("Nachricht: {} wird geladen", msg_id);
        return ResponseEntity.status(200).body(messageService.getSingleMessage(msg_id));

    }

}
