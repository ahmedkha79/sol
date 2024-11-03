package de.hamburg.sol.vs.server.instance;

import de.hamburg.sol.vs.server.model.Component;
import de.hamburg.sol.vs.utils.UUIDGenerator;

import java.net.InetAddress;
import static de.hamburg.sol.vs.server.config.GlobalConfig.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//Singleton
public class SolServer {
    private static SolServer instance;
    private final int comUUID;
    private LocalDateTime initializationTime;
    private int activeComponents;
    private int maxComponents;
    private Component solComponent;
    private String starUUID;
    private Map<Integer, Component> components;


    private SolServer(int maxComponents){
        this.comUUID = UUIDGenerator.generateCOM_UUID();
        this.initializationTime = LocalDateTime.now();
        this.activeComponents = 1;
        this.maxComponents = maxComponents;
        components = new HashMap<>();
        this.solComponent = new Component(comUUID, getLocalHostAddress(), getStar_Port());
        this.starUUID = generateStar_UUID();
        components.put(comUUID, solComponent);

    }

    public SolServer getInstance(int maxComponents){
        if(instance == null){
           instance = new SolServer(maxComponents);
        }
        return instance;
    }


    private String generateStar_UUID(){
        try {
            String input = solComponent.getIpAddress() + getGroupId() + comUUID;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder builder = new StringBuilder();
            for(byte b : hashBytes){
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null; //Eventuell Default-IpAdresse
        }
    }


}
