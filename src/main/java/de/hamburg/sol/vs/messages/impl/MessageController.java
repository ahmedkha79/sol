package de.hamburg.sol.vs.messages.impl;


import de.hamburg.sol.vs.messages.api.MessageHandler;
import de.hamburg.sol.vs.messages.datatype.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vs/v2/messages")
public class MessageController {

    private final MessageHandler messageHandler;

    public MessageController(@Lazy MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @PostMapping("")
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        return messageHandler.handlePostMessageRequest(message);
    }

    @PostMapping("/{msg_id}")
    public ResponseEntity<String> sendMessage(@PathVariable("msg_id") String msgId, @RequestBody Message message) {
        return messageHandler.handleReceivedPostMessageRequest(msgId, message);
    }

    @DeleteMapping("/{msg_id}")
    public ResponseEntity<String> deleteMessage(@PathVariable("msg_id") String msg_id, @RequestParam String star) {
        return messageHandler.handleDeleteMessage(msg_id, star);
    }

    @GetMapping()
    public ResponseEntity<?> getAllMessages(@RequestParam(value = "star", defaultValue = "all") String star,
                                                 @RequestParam(value = "scope", defaultValue = "active") String scope,
                                                 @RequestParam(value = "view", defaultValue = "id") String view) {

        return messageHandler.handleGetMessageRequest(star, scope, view);

    }

    @GetMapping("/{msg_id}")
    public ResponseEntity<?> getSingleMessage(@PathVariable("msg_id") String msg_id,
                                                   @RequestParam String star) {

        return messageHandler.handleGetSingleMessage(msg_id, star);
    }
}
