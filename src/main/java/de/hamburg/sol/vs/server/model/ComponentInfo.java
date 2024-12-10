package de.hamburg.sol.vs.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Data
@Log4j2
public class ComponentInfo {



    private String comUUID;
    private String ipAddress;
    private int tcpPort;
    private LocalDateTime integrationPort;
    private LocalDateTime lastInteraction;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
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

    public  void startTimeout(long timeout, TimeUnit unit, Runnable onTimeout){
        this.onTimeout = onTimeout;
        resetTimeout(timeout, unit);

    }

    public synchronized void resetTimeout(long timeout, TimeUnit unit) {
        log.info("Timer wird versucht zurückzusetzen");
        try {
            if (scheduledFuture != null && !scheduledFuture.isDone()) {
                log.info("Before cancel: isCancelled={}, isDone={}", scheduledFuture.isCancelled(), scheduledFuture.isDone());
                scheduledFuture.cancel(false);
                log.info("After cancel: isCancelled={}, isDone={}", scheduledFuture.isCancelled(), scheduledFuture.isDone());

            }

            scheduledFuture = scheduler.schedule(() -> {
                if (onTimeout != null) {
                    onTimeout.run();
                }
            }, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to reset timeout", e);
        }
    }

    public void stopTimeout(){
        scheduler.shutdownNow();
    }

}
