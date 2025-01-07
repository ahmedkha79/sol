package de.hamburg.sol.vs.config.multiplePortConfiguration;

import de.hamburg.sol.vs.config.global.GlobalConfig;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.TomcatServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class MultiPortConfiguration {


    private String serverPort = String.valueOf(GlobalConfig.getStarPort());

    private String galaxyPort = String.valueOf(GlobalConfig.getGalaxyPort());

    @Value("${server.galaxyPath")
    private String galaxyPath;

    public MultiPortConfiguration() throws IllegalAccessException {
    }

    @Bean
    public WebServerFactoryCustomizer servletContainer(){
        Connector[] additionalConnectors = this.additionalConnector();
        ServerProperties serverProperties = new ServerProperties();
        return new TomcatMultiConnectorServletWebServerFactoryCustomizer(serverProperties, additionalConnectors);
    }

    private Connector[] additionalConnector() {
        if (StringUtils.isEmpty(this.galaxyPort) || "null".equals(this.galaxyPort)) {
            return null;
        }

        Set<String> defaultPorts = new HashSet<>();
        defaultPorts.add(this.serverPort);

        if(!defaultPorts.contains(this.galaxyPort)) {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setScheme("http");
            connector.setPort(Integer.parseInt(this.galaxyPort));
            return new Connector[]{connector};
        } else {
            return new Connector[]{};
        }
    }



    private class TomcatMultiConnectorServletWebServerFactoryCustomizer extends TomcatServletWebServerFactoryCustomizer {
        private final Connector[] additionalConnectors;

        public TomcatMultiConnectorServletWebServerFactoryCustomizer(ServerProperties serverProperties, Connector[] additionalConnectors) {
            super(serverProperties);
            this.additionalConnectors = additionalConnectors;
        }

        @Override
        public void customize(TomcatServletWebServerFactory tomcatServletWebServerFactory) {
            super.customize(tomcatServletWebServerFactory);

            if(additionalConnectors != null && additionalConnectors.length > 0) {
                tomcatServletWebServerFactory.addAdditionalTomcatConnectors(additionalConnectors);

            }
        }
    }



}


