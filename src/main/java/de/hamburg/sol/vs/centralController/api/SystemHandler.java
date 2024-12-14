package de.hamburg.sol.vs.centralController.api;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;

public interface SystemHandler {

    ResponseEntity<String> handleDeleteRequest(String comUUID, String star);

    ResponseEntity<String> handleGetRequest(String comUUID, String star);

}
