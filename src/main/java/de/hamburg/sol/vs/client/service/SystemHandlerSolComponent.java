package de.hamburg.sol.vs.client.service;

import de.hamburg.sol.vs.centralController.api.SystemHandler;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Lazy
@Log4j2
public class SystemHandlerSolComponent implements SystemHandler {


    private ApplicationContext applicationContext;

//    @Qualifier("solComponentDynamic")
    private SolComponent solComponent;



    public SystemHandlerSolComponent(ApplicationContext applicationContext, @Lazy SolComponent solComponent) {
        this.applicationContext = applicationContext;
        this.solComponent = solComponent;
    }



    @Override
    public ResponseEntity<String> handleDeleteRequest(String comUUID, String star) {
        return handleDeleteAsComponent(comUUID, star);
    }

    @Override
    public ResponseEntity<String> handleGetRequest(String comUUID, String star) {
        return handleGetAsComponent(comUUID, star);
    }

    private ResponseEntity<String> handleGetAsComponent(String comUUID, String star) {
        log.info("GET Anfrage erhalten für Solkomponente: {}", solComponent.getComUUID());

        if(comUUID == null || comUUID.isEmpty()){
            log.warn("comUUID ist nicht angegeben");
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!comUUID.equals(solComponent.getComUUID())){
            log.warn("Falsche comUUID angegeben");
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        if(star == null || star.isEmpty()){
            log.warn("star ist nicht angegeben");
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!solComponent.getStarUUID().equals(star)){
            log.warn("Falsche solUUID angegeben");
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        SolProtocol solProtocol = solComponent.getComponentInfo();
        solProtocol.setStatus("200");

        String protocol = "";

        try {
            protocol = ProtocolHandler.writeValueAsString(solProtocol);

        } catch (Exception e){
            log.warn("Fehler beim Aussenden der JSON Entität");
        }
        log.info("Solkomponente: {} meldet sich zurück", comUUID);

        return ResponseEntity.ok(protocol);

    }


    private ResponseEntity<String> handleDeleteAsComponent(String comUUID, String star){
        if(!solComponent.getStarUUID().equals(star) || solComponent.getComUUID().equals(comUUID)){
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        solComponent.terminateComponent();

        return ResponseEntity.status(200).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}
