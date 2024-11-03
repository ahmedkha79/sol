package de.hamburg.sol.vs.server.model;

import java.time.LocalDateTime;

public class Component {


    private int comUUID;
    private String ipAddress;
    private int tcpPort;
    private LocalDateTime integrationPort;
    private LocalDateTime lastInteraction;

    public Component(int comUUID, String ipAddress, int tcpPort){
        this.comUUID = comUUID;
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.integrationPort = LocalDateTime.now();
        this.lastInteraction = LocalDateTime.now();
    }


    public int getComUUID() {
        return comUUID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getTcpPort() {
        return tcpPort;
    }
}
