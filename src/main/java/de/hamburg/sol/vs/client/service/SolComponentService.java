package de.hamburg.sol.vs.client.service;


import de.hamburg.sol.vs.client.model.SolComponent;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Log4j2

public class SolComponentService {


    private final SolComponent solComponent;

    private  RestTemplate restTemplate;
    public SolComponentService(@Lazy SolComponent solComponent, RestTemplate restTemplate) {
        this.solComponent = solComponent;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    public void sendHealth(){
        try {
            log.info("Sending health check");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
