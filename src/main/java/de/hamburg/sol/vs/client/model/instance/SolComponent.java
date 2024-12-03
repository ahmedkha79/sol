package de.hamburg.sol.vs.client.model.instance;

import de.hamburg.sol.vs.protocol.SolProtocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

}






