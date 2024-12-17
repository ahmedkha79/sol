package de.hamburg.sol.vs.server.service;

import de.hamburg.sol.vs.centralController.api.SystemHandler;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Lazy
public class SystemHandlerSolServer implements SystemHandler {


    private final SolServer solServer;


    public SystemHandlerSolServer(@Lazy SolServer solServer) {
        this.solServer = solServer;
    }





    @Override
    public ResponseEntity<String> handleDeleteRequest(String comUUID, String star) {
        return handleDeleteAsServer(comUUID, star);
    }

    @Override
    public ResponseEntity<String> handleGetRequest(String comUUID, String star) {
        log.info("GET Anfrage von der Komponente: {}", comUUID);

        if(!checkComUUID(comUUID)){
            log.warn("comUUID: {} nicht im Sternensystem registriert", comUUID);
        }

        if(!star.equals(solServer.getStarUUID())){
            log.warn("Inkorrekte starUUID: {}", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        ComponentInfo ci = solServer.getComponentInfo(comUUID);
        if(ci == null){
            log.warn("Komponente mit der comUUID: {} nicht gefunden", comUUID);
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        SolProtocol solProtocol = solServer.getComponentInfoAsSolProtocol(ci);
        solProtocol.setStatus("200");
        String protocol = "";
        try {

            protocol = ProtocolHandler.writeValueAsString(solProtocol);

        } catch (Exception e){
            log.error(e);
        }

        return ResponseEntity.status(200).body(protocol);


    }

    @Override
    public void handleExitCommand() {
        solServer.handleExitCommand();
    }

    private boolean checkComUUID(String comUUID) {
        if(comUUID == null || comUUID.isEmpty()){
            log.warn("comUUID nicht mit angegeben");
            return false;
        }
        return true;
    }

    private boolean checkStarUUID(String starUUID){
        if(starUUID == null || starUUID.isEmpty() || !solServer.getStarUUID().equals(starUUID)){
            log.warn("starUUID nicht mit angegeben oder falsch");
            return false;
        }

        return true;
    }




    private ResponseEntity<String> handleDeleteAsServer(String comUUID, String star){

        if(!checkComUUID(comUUID)){
            log.warn("Keine Solkomponente: {} gefunden", comUUID);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!checkStarUUID(star)){
            log.warn("Inkorrekte : {}", star);
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        ComponentInfo componentInfo = solServer.getComponentInfo(comUUID);

        if(componentInfo == null){
            log.warn("Solkomponente: {} nicht Teil des Sternensystems", comUUID);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }



        if(!componentInfo.getStatus().equals("200")){
            log.info("Status der Komponente ist: ", componentInfo.getStatus());
            log.info("SolComponent {} ist inaktiv oder konnte nicht gefunden werden", comUUID);
            return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
        }

        componentInfo.updateLastInteraction();
        componentInfo.setStatus("left");

        log.info("SolComponent {} wurde erfolgreich aus dem Stern entfernt", comUUID);
        return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());

    }


}
