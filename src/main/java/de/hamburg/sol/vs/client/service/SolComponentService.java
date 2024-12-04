package de.hamburg.sol.vs.client.service;


import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
@Log4j2
@Lazy
public class SolComponentService {



    private SolComponent solComponent;


    private final RestTemplate restTemplate;

    @Autowired
    public SolComponentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void initialize(SolComponent solComponent){
        this.solComponent = solComponent;
    }


    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void sendPatchBeatChecks(){
        int retries = 0;
        boolean patchSuccessful = false;
        int timeOut = 10000;
        //RestTemplate restTemplateWithTimeout = createRestTemplateWithTimeout(5000);
        while(retries < 2 && !patchSuccessful){
            try{
                log.info("Solkomponente: {} sende Patch Beat Check", solComponent.getComPort());

                SolProtocol solProtocol = createSolProtocol();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(List.of(MediaType.TEXT_PLAIN));

                String patchUrl = String.format("http://%s:%d/vs/v1/system/%s",solComponent.getSolIpAddress(), solComponent.getSolPort(), solComponent.getComUUID());
                //String patchUrl = solComponent.getRestApiUrl() + "/" + solComponent.getComUUID();
                log.info("Sende an folgende URL {}", patchUrl);

                HttpEntity<SolProtocol> patchEntity = new HttpEntity<>(solProtocol, headers);
                ResponseEntity<String> patchRequest = restTemplate.exchange(patchUrl, HttpMethod.PATCH, patchEntity, String.class);
                log.info("Heartbeat erfolgreich an Sol gesendet");

                if(patchRequest.getStatusCode() == HttpStatus.OK){
                    log.info("PATCH-Request erfolgreich angenommen");
                    patchSuccessful = true;
                } else {
                    log.warn("Unerwarteter Statuscode: {}", patchRequest.getStatusCode());
                }

            } catch (RestClientException e){
                log.error("Fehler beim Senden der Anfrage: {}", e.getMessage());
            }

            if(!patchSuccessful){
                retries++;
                log.info("Fehler beim Senden der Anfrage");
                waitBeforeRetry(timeOut);
            }
        }

        if(!patchSuccessful){
            log.error("Verbindung zu Sol konnte nach {} Versuchen nicht hergestellt werden", retries);
            //TODO terminateComponent
            terminateComponent();
        }
    }

    private void waitBeforeRetry(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("Fehler beim Warten: {}", e.getMessage());
        }
    }

    private SolProtocol createSolProtocol(){

        SolProtocol protocol = SolProtocol.builder()
                .star(solComponent.getStarUUID())
                .sol(solComponent.getSolUUID())
                .comUUID(solComponent.getComUUID())
                .port(solComponent.getComPort())
                .ipAddress(solComponent.getComIpAddress())
                .status("200")
                .build();

        return protocol;
    }

    private void terminateComponent(){
        log.error("Komponente {} wird beendet.", solComponent.getComUUID());
        System.exit(1);
    }

}
