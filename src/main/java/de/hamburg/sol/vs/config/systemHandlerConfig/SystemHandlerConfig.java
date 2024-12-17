package de.hamburg.sol.vs.config.systemHandlerConfig;

import de.hamburg.sol.vs.centralController.api.SystemHandler;
import de.hamburg.sol.vs.client.service.SystemHandlerSolComponent;
import de.hamburg.sol.vs.server.service.SystemHandlerSolServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SystemHandlerConfig {


    @Bean
    @Lazy
    public SystemHandler systemHandler (ApplicationContext context) {
        if(context.containsBean("dynamicSolComponent")) {
            return context.getBean(SystemHandlerSolComponent.class);
        } else {

            return context.getBean(SystemHandlerSolServer.class);
        }

    }

}

