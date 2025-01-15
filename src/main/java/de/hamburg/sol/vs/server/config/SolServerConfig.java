package de.hamburg.sol.vs.server.config;

import de.hamburg.sol.vs.galaxy.model.GalaxyModel;
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

    private final GalaxyModel galaxyModel;

    public SolServerConfig(ScheduledExecutorService scheduler, RestTemplate restTemplate, GalaxyModel galaxyModel) {
        this.scheduler = scheduler;
        this.restTemplate = restTemplate;
        this.galaxyModel = galaxyModel;
    }
    @Bean
    @Lazy
    public SolServer solServer() throws SocketException, IllegalAccessException {
        return new SolServer(scheduler,4, restTemplate, galaxyModel);
    }
}
