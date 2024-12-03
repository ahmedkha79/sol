package de.hamburg.sol.vs.central.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/vs/v1/system")
@Log4j2
@Async
public class SolComponentController {



    private Map<String, SolComponent> components = new ConcurrentHashMap<>();


    public void initialize(SolComponent solComponent) {
        components.put(solComponent.getComUUID(), solComponent);
        log.info("Component: {}", solComponent.getComUUID());
        log.info("Map Size: {}", components.size());
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
}
