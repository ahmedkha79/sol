package de.hamburg.sol.vs.client.model.instance;

import de.hamburg.sol.vs.protocol.SolProtocol;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
@Lazy
@Component
public class SolComponent {

    private String starUUID;
    private String solUUID;
    private String solIpAddress;
    private int solPort;

    private String comUUID;
    private String comIpAddress;
    private int comPort;




    public SolProtocol getComponentInfo(){
        return SolProtocol.builder()
                .star(starUUID)
                .sol(solUUID)
                .comUUID(comUUID)
                .ipAddress(comIpAddress)
                .port(comPort)
                .build();
    }

    public void terminateComponent(){
        log.info("Komponente: {} wird abgeschaltet", comUUID);
        System.exit(0);
    }

}






