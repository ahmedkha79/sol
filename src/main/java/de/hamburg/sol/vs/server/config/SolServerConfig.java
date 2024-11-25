package de.hamburg.sol.vs.server.config;

import de.hamburg.sol.vs.server.instance.SolServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.net.SocketException;

@Configuration
public class SolServerConfig {

    @Bean
    @Lazy
    public SolServer solServer() throws SocketException, IllegalAccessException {
        return new SolServer(4);
    }
}
