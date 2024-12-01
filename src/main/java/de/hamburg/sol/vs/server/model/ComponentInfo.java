package de.hamburg.sol.vs.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
public class ComponentInfo {



    private String comUUID;
    private String ipAddress;
    private int tcpPort;
    private LocalDateTime integrationPort;
    private LocalDateTime lastInteraction;
    private final ScheduledExecutorService scheduler;
    private String status;
    private Runnable onTimeout;

    public ComponentInfo(String comUUID, String ipAddress, int tcpPort){
        this.comUUID = comUUID;
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.integrationPort = LocalDateTime.now();
        this.lastInteraction = LocalDateTime.now();
    }

    public void updateLastInteraction(){
        this.lastInteraction = LocalDateTime.now();
    }

    public void startTimeout(long timeout, TimeUnit unit, Runnable onTimeout){
        this.onTimeout = onTimeout;
        resetTimeout(timeout, unit);

    }

    public void resetTimeout(long timeout, TimeUnit unit){

        scheduler.schedule(() -> {
            if(onTimeout != null){
                onTimeout.run();
            }
        }, timeout, unit);
    }

    public void stopTimeout(){
        scheduler.shutdownNow();
    }

}
