package de.hamburg.sol.vs.server.config;

import de.hamburg.sol.vs.server.instance.SolServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SolServerConfig {

    private final ScheduledExecutorService scheduler;
    public SolServerConfig(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }
    @Bean
    @Lazy
    public SolServer solServer() throws SocketException, IllegalAccessException {
        return new SolServer(scheduler,4);
    }
}
