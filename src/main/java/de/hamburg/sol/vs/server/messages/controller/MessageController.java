package de.hamburg.sol.vs.server.messages.controller;

import de.hamburg.sol.vs.server.messages.datatype.Message;
import de.hamburg.sol.vs.server.messages.datatype.MessageList;
import de.hamburg.sol.vs.server.messages.logic.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("vs/v1/messages")
@Log4j2
public class MessageController {

    private final MessageService messageService;

    public MessageController(@Lazy final MessageService messageService) {
        this.messageService = messageService;
    }


    @PostMapping("")
    public ResponseEntity<String> createMessage(@RequestBody Message message) {
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


        Message processdMessage = messageService.processMessage(message);
        log.info("Nachricht wurde von Sol aufgenommen und gespeichert");

        return ResponseEntity.status(200).body(processdMessage.getMsg_id());


    }

    @DeleteMapping("/{msg_id}")
    public ResponseEntity<?> deleteMessage(@PathVariable String msg_id, @RequestParam String star){
        if(!messageService.validateStar(star)){
            log.info("star: {} ist inkorrekt oder nicht angegeben", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(messageService.checkIfMsgIDIsEmpty(msg_id) || !messageService.checkMessageWithID(msg_id)){
            log.info("MSG_ID: {} ist leer sein oder existiert nicht", msg_id);
            return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
        }

        if(messageService.deleteMessage(msg_id)){
            return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());
        }
            log.warn("Nachricht konnte nicht gelöscht werden");
            return ResponseEntity.status(412).body(HttpStatus.PRECONDITION_FAILED.getReasonPhrase());


    }


    @GetMapping()
    public ResponseEntity<?> getMessages(@RequestParam String star,
                                         @RequestParam(value = "scope", defaultValue = "active") String scope,
                                         @RequestParam(value = "view", defaultValue = "id") String view){

        if(!messageService.validateStar(star)){
            log.info("star: {} ist inkorrekt oder nicht angegeben", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        log.info("scope: {} und view: {}", scope,view);

        MessageList messageList = messageService.getMessages(star, scope, view);

        return ResponseEntity.status(200).body(messageList);


    }

    @GetMapping("/{msg_id}")
    public ResponseEntity<?> getSingleMessage(@PathVariable String msg_id,
                                              @RequestParam String star){
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
