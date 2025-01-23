package de.hamburg.sol.vs.centralController.api;

import de.hamburg.sol.vs.server.instance.*;
import de.hamburg.sol.vs.client.model.instance.*;
import org.springframework.http.ResponseEntity;

/**
 * Zentrales Interface, ermöglicht unterschiedliche Implementierungen, so wohl für {@link SolComponent } und {@link SolServer}
 */

public interface SystemHandler {

    ResponseEntity<String> handleDeleteRequest(String comUUID, String star);

    ResponseEntity<String> handleGetRequest(String comUUID, String star);

   void handleExitCommand();

}
