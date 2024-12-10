package de.hamburg.sol.vs.central.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/vs/v1/system")
@Log4j2
@Lazy
public class SolComponentController {


    @Autowired
    private ApplicationContext context;

    private Map<String, SolComponent> components = new ConcurrentHashMap<>();

    private SolComponent solComponent;


    private SolServer solServer;





    public void initialize(SolComponent solComponent) {
        components.put(solComponent.getComUUID(), solComponent);
        log.info("Component: {}", solComponent.getComUUID());
        log.info("Map Size: {}", components.size());

        this.solComponent = solComponent;
        if(!solComponent.isComponent()){
            this.solServer = context.getBean(SolServer.class);
        }
    }



    @GetMapping("/{comUUID}")
    public ResponseEntity<String> getComponent(@PathVariable String comUUID,
                                               @RequestParam() String star) throws JsonProcessingException {

        log.info("GET Anfrage erhalten für Solkomponente: {}", comUUID);

        if(comUUID == null || comUUID.isEmpty()) {
            log.warn("comUUID nicht mit angegeben");
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }


        if (!components.containsKey(comUUID)) {
            log.warn("Solkomponente mit {} existiert nicht", comUUID);
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        SolComponent solComponent = components.get(comUUID);

        if(!solComponent.getStarUUID().equals(star)) {
            log.warn("Solkomponente nicht Teil des Sternensystems: {}", star);
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }


        SolProtocol solProtocol = solComponent.getComponentInfo();
        solProtocol.setStatus("200");

        String protocol = ProtocolHandler.writeValueAsString(solProtocol);

        log.info("Solkomponente: {} meldet sich erfolgreich zurück", comUUID);

        return ResponseEntity.ok(protocol);


    }

    @DeleteMapping("/{comUUID}")
    public ResponseEntity<String> disconnectComponent(@PathVariable String comUUID, @RequestParam() String star,
                                                      @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor, HttpServletRequest request) {

        log.info("DELETE Request von der Komponente {} erhalten", comUUID);
        if(solComponent.isComponent()){
            log.info("Behandele den DELETE als SolComponent");
            return handleDeleteAsComponent(comUUID, star);

        } else {
            log.info("Behandele den DELETE als SolServer");
           return handleDeleteAsServer(comUUID, star, forwardedFor, request);
        }
    }

    private ResponseEntity<String> handleDeleteAsServer(String comUUID, String star, String forwardedFor, HttpServletRequest request){


        ComponentInfo componentInfo = solServer.getComponentInfo(comUUID);

        log.info("Werte die IP-Adresse der SolComponent aus: {}", comUUID);
        String requestIp = (forwardedFor != null) ? forwardedFor : request.getRemoteAddr();
        log.info("Folgende IP-Adresse {} der SolComponent {}", requestIp, comUUID);
        if(!componentInfo.getIpAddress().equals(requestIp) || !solServer.getStarUUID().equals(star)){
            log.warn("IP Adresse war {}, sollte aber {} sein", requestIp, componentInfo.getIpAddress());
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!componentInfo.getStatus().equals("200")){
            log.info("SolComponent {} ist inaktiv oder konnte nicht gefunden werden", comUUID);
            return ResponseEntity.status(404).body(HttpStatus.NOT_FOUND.getReasonPhrase());
        }

        componentInfo.updateLastInteraction();
        componentInfo.setStatus("left");

        log.info("SolComponent {} wurde erfolgreich aus dem Stern entfernt", comUUID);
        return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());

    }

    private ResponseEntity<String> handleDeleteAsComponent(String comUUID, String star){
        if(!solComponent.getStarUUID().equals(star) || solComponent.getComUUID().equals(comUUID)){
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        solComponent.terminateComponent();

        return ResponseEntity.status(200).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }




}
