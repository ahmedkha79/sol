package de.hamburg.sol.vs.messages.api;

import de.hamburg.sol.vs.messages.datatype.Message;
import org.springframework.http.ResponseEntity;

public interface MessageHandler {

     ResponseEntity<String> handlePostMessageRequest(Message message);

     ResponseEntity<String> handleReceivedPostMessageRequest(String msg_id, Message message);

     ResponseEntity<?> handleGetMessageRequest(String star, String scope, String view);

     ResponseEntity<?> handleGetSingleMessage(String msg_id, String star);

     ResponseEntity<String> handleDeleteMessage(String msg_id, String star);


}
