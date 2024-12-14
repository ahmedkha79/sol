package de.hamburg.sol.vs.server.config;

import de.hamburg.sol.vs.server.instance.SolServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SolServerConfig {

    private final ScheduledExecutorService scheduler;

    private final RestTemplate restTemplate;

    public SolServerConfig(ScheduledExecutorService scheduler, RestTemplate restTemplate) {
        this.scheduler = scheduler;
        this.restTemplate = restTemplate;
    }
    @Bean
    @Lazy
    public SolServer solServer() throws SocketException, IllegalAccessException {
        return new SolServer(scheduler,4, restTemplate);
    }
}
