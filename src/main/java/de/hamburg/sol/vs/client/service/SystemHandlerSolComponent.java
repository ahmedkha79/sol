package de.hamburg.sol.vs.client.service;

import de.hamburg.sol.vs.centralController.api.SystemHandler;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
@Lazy
@Log4j2
public class SystemHandlerSolComponent implements SystemHandler {



    private SolComponent solComponent;

    private RestTemplate restTemplate;



    public SystemHandlerSolComponent(RestTemplate restTemplate, @Lazy SolComponent solComponent) {
        this.solComponent = solComponent;
        this.restTemplate = restTemplate;
    }



    @Override
    public ResponseEntity<String> handleDeleteRequest(String comUUID, String star) {
        return handleDeleteAsComponent(comUUID, star);
    }

    @Override
    public ResponseEntity<String> handleGetRequest(String comUUID, String star) {
        return handleGetAsComponent(comUUID, star);
    }

    @Override
    public void handleExitCommand() {

            String url = String.format("http://%s:%d/vs/v1/system/%s?star=%s",
                    solComponent.getSolIpAddress(),
                    solComponent.getSolPort(),
                    solComponent.getComUUID(),
                    solComponent.getStarUUID()
            );

            log.info("Url: {} wurde für das Abmelden erzeugt", url);
            int retries = 0;
            int timeOut = 10000;
            boolean disconnectSuccessful = false;



            while(retries <= 1 && !disconnectSuccessful){
                try {

                    HttpHeaders headers = new HttpHeaders();
                    headers.setAccept(List.of(MediaType.TEXT_PLAIN));
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<String> deleteRequest = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

                    log.info("Sende DELETE Request raus");

                    if(deleteRequest.getStatusCode().is2xxSuccessful()){
                        log.info("Erfolgreich von Sol abgemeldet");
                        disconnectSuccessful = true;

                    } else {
                        log.warn("Sol antwortete mit Status: {}", deleteRequest.getStatusCode());
                        retries++;
                        waitBeforeRetry(timeOut);
                    }


                } catch (Exception e) {
                    log.error("Fehler beim Abmelden von Sol");
                    retries++;
                    log.error(e.getMessage());
                }
            }

            if(!disconnectSuccessful){
                log.info("SOL konnte nicht erreicht werden, bereite Abschaltung vor...");

            }

            solComponent.terminateComponent();



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

    private void waitBeforeRetry(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("Fehler beim Warten: {}", e.getMessage());
        }
    }
}
