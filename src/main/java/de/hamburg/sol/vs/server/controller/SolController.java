package de.hamburg.sol.vs.server.controller;


import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;


import static de.hamburg.sol.vs.utils.InetAddressHandler.isIpReachable;
@Log4j2
@RestController
@RequestMapping("/vs/v1/system")
@Async
public class SolController {

    private final SolServer solServer;

    public SolController(@Lazy SolServer solServer) {
        this.solServer = solServer;
    }



    @PostMapping("")
    public ResponseEntity<String> registerComponent(@RequestBody SolProtocol solRequest) throws Exception {
        if(!isAuthorized(solRequest)) {
            return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }

        if(!solServer.freeSpace()){
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).body("no room left");
        }

        int timeout = 4000;
        if(!isIpReachable(solRequest.getIpAddress(), solRequest.getPort(), timeout) || !solServer.queueContainsComUUID(solRequest.getComUUID())){
            return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
        }

        ComponentInfo componentInfo = new ComponentInfo(solRequest.getComUUID(), solRequest.getIpAddress(), solRequest.getPort());
        solServer.addComponent(componentInfo);
        return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());


    }

    @PatchMapping("/{comUUID:\\d+}")
    public ResponseEntity<String> updateComponent(@RequestBody SolProtocol solRequest, @PathVariable String comUUID) throws Exception {
            log.info("Patchanfrage von der Solkomponente mit der comUUID: {}", comUUID);
            if(!isAuthorized(solRequest)) {
                log.info("Unautorisierte Anfrage: Sterninformationen stimmen nicht");
                return ResponseEntity.status(401).body(HttpStatus.UNAUTHORIZED.getReasonPhrase());


            }

            if(!solServer.checkIfComponentExists(comUUID) || !"200".equals(solRequest.getStatus())){
                log.info("Solkomponente mit folgender comUUID {} wurde nicht gefunden", comUUID);
                return ResponseEntity.status(404).body(HttpStatus.CONFLICT.getReasonPhrase());
            }

            ComponentInfo componentInfo = solServer.getComponentInfo(comUUID);

            if(!componentInfo.getIpAddress().equals(solRequest.getIpAddress())) {
                log.info("Ip Adresse sollte {}, ist aber {} ", componentInfo.getIpAddress(), solRequest.getIpAddress());
                return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());

            } else if(componentInfo.getTcpPort() != solRequest.getPort()) {
                log.info("Port sollte {}, ist aber {} ", componentInfo.getTcpPort(), solRequest.getPort());

                return ResponseEntity.status(409).body(HttpStatus.CONFLICT.getReasonPhrase());
            }

            solServer.updateComponentLastSeen(comUUID);
            componentInfo.setStatus("200");
            log.info("comUUID: {} wurde erfolgreich gepatched", comUUID);

            return ResponseEntity.status(200).body(HttpStatus.OK.getReasonPhrase());
    }





    private boolean isAuthorized(SolProtocol solRequest) throws Exception {
        if(solRequest == null || !solServer.getStarUUID().equals(solRequest.getStar())) {
            return false;
        }
        return true;
    }



}
