package de.hamburg.sol.vs.server.messages.controller;

import de.hamburg.sol.vs.server.messages.datatype.Message;
import de.hamburg.sol.vs.server.messages.logic.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if(!messageService.validateStar(message)){
            log.info("star: {} ist inkorrekt oder nicht angegeben", message.getStar());
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!messageService.validateFormat(message)){
            log.info("Format der Nachricht ist inkorrekt, überprüfe Subject: {} und Origin: {}", message.getSubject(), message.getOrigin());
            return ResponseEntity.status(412).body(HttpStatus.PRECONDITION_FAILED.getReasonPhrase());
        }

        if(!messageService.checkIfMsgIDIsEmpty(message)){
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
}
