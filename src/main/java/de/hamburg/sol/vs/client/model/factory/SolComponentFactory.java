package de.hamburg.sol.vs.client.model.factory;


import de.hamburg.sol.vs.central.controller.SolComponentController;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.client.service.SolComponentService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SolComponentFactory {


    private final ApplicationContext applicationContext;

    public SolComponentFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public SolComponent createSolComponent(String starUUID, String solUUID, String solIpAddress, int solPort,
                                           String comUUID, String comIpAddress, int comPort, boolean component) {
        SolComponent solComponent = SolComponent.builder()
                .starUUID(starUUID)
                .solUUID(solUUID)
                .solIpAddress(solIpAddress)
                .solPort(solPort)
                .comUUID(comUUID)
                .comIpAddress(comIpAddress)
                .comPort(comPort)
                .build();
        if(component) {
            SolComponentService service = applicationContext.getBean(SolComponentService.class);
            service.initialize(solComponent);
        }
        SolComponentController controller = applicationContext.getBean(SolComponentController.class);
        controller.initialize(solComponent);
        return solComponent;
    }
}
