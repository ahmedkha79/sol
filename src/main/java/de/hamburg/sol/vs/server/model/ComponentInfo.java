package de.hamburg.sol.vs.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class ComponentInfo {



    private String comUUID;
    private String ipAddress;
    private int tcpPort;
    private LocalDateTime integrationPort;
    private LocalDateTime lastInteraction;
    private String status;

    public ComponentInfo(String comUUID, String ipAddress, int tcpPort){
        this.comUUID = comUUID;
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.integrationPort = LocalDateTime.now();
        this.lastInteraction = LocalDateTime.now();
    }

}
