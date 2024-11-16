package de.hamburg.sol.vs.server.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hamburg.sol.vs.server.instance.response.SolResponse;
import de.hamburg.sol.vs.server.model.Component;
import de.hamburg.sol.vs.utils.UUIDGenerator;
import lombok.Getter;
import lombok.Setter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import static de.hamburg.sol.vs.server.config.GlobalConfig.*;

import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

//Singleton
public class SolServer implements Runnable{
    private static SolServer instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Getter
    private final int comUUID;
    private LocalDateTime initializationTime;
    private int activeComponents;
    private int maxComponents;
    private Component solComponent;
    private final String starUUID;
    @Setter
    private DatagramSocket udpSocket;
    //Thread-safe
    private ConcurrentHashMap<Integer, Component> components = new ConcurrentHashMap<>();
    private boolean running;



    private SolServer(int maxComponents) throws IllegalAccessException, SocketException {
        this.comUUID = generateCOM_UUID();
        this.initializationTime = LocalDateTime.now();
        this.activeComponents = 1;
        this.maxComponents = maxComponents;
        this.solComponent = new Component(comUUID, getLocalHostAddress(), getStarPort());
        this.starUUID = generateStar_UUID();
        this.udpSocket = new DatagramSocket(getStarPort());
        components.put(comUUID, solComponent);
        this.running = true;
    }

    public static SolServer getInstance(int maxComponents) throws IllegalAccessException, SocketException {
        if (instance == null) {
            instance = new SolServer(maxComponents);
        }
        return instance;
    }




    @Override
    public void run() {
        listenForBroadcastsRequests();
    }

    public void stopServer(){
        this.running = false;
    }

    public void listenForBroadcastsRequests(){
        try {
                System.out.println("SOL lauscht auf Broadcasts am Port " + getStarPort());
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Empfangene Nachricht: " + request);
                if(request.equals("HELLO?")){
                    respondToHello(packet.getAddress(), packet.getPort());
                } else {
                    System.out.println("Unknown request: " + request);
                }

        } catch (Exception e) {
           e.printStackTrace();
        }
    }



    private void respondToHello(InetAddress address, int port){
        try{
            SolResponse response = new SolResponse(starUUID, comUUID, solComponent.getIpAddress(), solComponent.getTcpPort(), generateCOM_UUID());
            String responseJson = objectMapper.writeValueAsString(response);
            byte[] responseData = responseJson.getBytes();

            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
            udpSocket.send(responsePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private int generateCOM_UUID(){
        int comUUID;
        do {
            comUUID = UUIDGenerator.generateCOM_UUID();
        } while(components.containsKey(comUUID));
       return comUUID;
    }

    public void addComponent(Component component){
        if(!components.containsKey(component)){
            components.put(comUUID, component);
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
