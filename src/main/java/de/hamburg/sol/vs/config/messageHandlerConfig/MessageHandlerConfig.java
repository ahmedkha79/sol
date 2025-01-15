package de.hamburg.sol.vs.config.messageHandlerConfig;

import de.hamburg.sol.vs.client.messages.MessageHandlerSolComponent;
import de.hamburg.sol.vs.messages.api.MessageHandler;
import de.hamburg.sol.vs.server.messages.controller.v2.MessageHandlerSolServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MessageHandlerConfig {

    @Bean
    @Lazy

    public MessageHandler messageHandler(ApplicationContext applicationContext) {
        if(applicationContext.containsBean("dynamicSolComponent")) {
            return applicationContext.getBean(MessageHandlerSolComponent.class);
        } else {
            return applicationContext.getBean(MessageHandlerSolServer.class);
        }
    }

}
