package de.hamburg.sol.vs.client.model.factory;


import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.client.service.SolComponentService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Component
public class SolComponentFactory {


    private final ApplicationContext applicationContext;

    public SolComponentFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public SolComponent createSolComponent(String starUUID, String solUUID, String solIpAddress, int solPort,
                                           String comUUID, String comIpAddress, int comPort) {
        SolComponent solComponent = SolComponent.builder()
                .starUUID(starUUID)
                .solUUID(solUUID)
                .solIpAddress(solIpAddress)
                .solPort(solPort)
                .comUUID(comUUID)
                .comIpAddress(comIpAddress)
                .comPort(comPort)
                .build();
        ((GenericWebApplicationContext) applicationContext).registerBean("dynamicSolComponent", SolComponent.class, () -> solComponent, bd -> bd.setPrimary(true));
        applicationContext.getBean(SolComponent.class);

        applicationContext.getBean(SolComponentService.class);

        return solComponent;
    }

}
