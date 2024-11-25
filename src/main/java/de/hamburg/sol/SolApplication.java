package de.hamburg.sol;

import de.hamburg.sol.vs.client.broadcast.BroadCastClient;


import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static de.hamburg.sol.vs.config.GlobalConfig.getStarPort;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class SolApplication {


    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    BroadCastClient broadCastClient;

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication application = new SpringApplication(SolApplication.class);
        System.out.println(System.getProperty("user.dir"));


        try{
            int port = getStarPort();
            application.setDefaultProperties(Map.of("server.port", port));
            log.info("Starte Application auf dem Port: {}", port);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        application.run(args);

    }

    @PostConstruct
    public void init(){
        try{
            createLogDirectory();
//            BroadCastClient broadCastClient = applicationContext.getBean(BroadCastClient.class);
            Thread broadCastThread = new Thread(broadCastClient);
            broadCastThread.setName("Peer 1");
            log.info("{} gestartet", broadCastThread.getName());
            broadCastThread.start();

            Thread.sleep(90000);

            BroadCastClient broadCastClient2 = applicationContext.getBean(BroadCastClient.class);
            Thread broadCastThread2 = new Thread(broadCastClient2);
            broadCastThread2.setName("Peer 2");
            broadCastThread2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createLogDirectory(){
        String workingDirectory = System.getProperty("user.dir");
        Path logDir = Paths.get(workingDirectory+"/logs");

        try{
            if(Files.notExists(logDir)){
                Files.createDirectories(logDir);
                System.out.println("Log-Ordner wurde erstellt: " + logDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Erstellen des Log-Ordners: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
