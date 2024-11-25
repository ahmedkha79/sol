package de.hamburg.sol.vs.server.instance;

import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.UUIDGenerator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import static de.hamburg.sol.vs.config.GlobalConfig.*;
import static de.hamburg.sol.vs.utils.InetAddressHandler.*;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import static de.hamburg.sol.vs.utils.ProtocolHandler.*;

//Singleton
public class SolServer implements Runnable {
    //private static SolServer instance;
    @Getter
    private final String comUUID;
    private LocalDateTime initializationTime;
    private int activeComponents;
    private int maxComponents;
    @Getter
    private ComponentInfo solComponentInfo;
    @Getter
    private final String starUUID;
    @Setter
    private DatagramSocket udpSocket;
    //Thread-safe
    private ConcurrentHashMap<String, ComponentInfo> components = new ConcurrentHashMap<>();
    //Registrierungsmap
    private ConcurrentLinkedQueue<String> comUUIDQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean running;
    private static final CountDownLatch latch = new CountDownLatch(1);



    public SolServer(int maxComponents) throws IllegalAccessException, SocketException {
        this.comUUID = generateCOM_UUID();
        this.initializationTime = LocalDateTime.now();
        this.activeComponents = 1;
        this.maxComponents = maxComponents;
        this.solComponentInfo = new ComponentInfo(comUUID, getLocalHostAddress(), getStarPort());
        this.solComponentInfo.setStatus("200");
        this.starUUID = generateStar_UUID();
        this.udpSocket = new DatagramSocket(getStarPort());
        components.put(comUUID, solComponentInfo);
        this.running = false;
    }


//    public static SolServer getInstance(int maxComponents) throws IllegalAccessException, SocketException {
//        if (instance == null) {
//            instance = new SolServer(maxComponents);
//        }
//        return instance;
//    }
//
//    public static SolServer get(){
//        return instance;
//    }

    public static boolean isServerReady(){
        return latch.getCount() == 0;
    }




    @Override
    public void run() {
        this.running = true;
        System.err.println("Solserver gestartet");
        latch.countDown();
        while(running) {
            listenForBroadcastsRequests();
        }
    }

    public void stopServer(){
        this.running = false;
        Thread.currentThread().interrupt();
        if(udpSocket != null){
            udpSocket.close();
        }
    }

    public void listenForBroadcastsRequests(){
        try {

                System.out.println("SOL lauscht auf Broadcasts am Port " + getStarPort());
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Empfangene Nachricht: " + request);
                if (request.equals("HELLO?")) {
                    respondToHello(packet.getAddress(), packet.getPort());
                } else {
                    System.out.println("Unknown request: " + request);
                }



        } catch (Exception e) {
           e.printStackTrace();
        }
    }



    @Async
    protected void respondToHello(InetAddress address, int port){
        try{
            String com_comUUID = generateCOM_UUID();
            SolProtocol response = new SolProtocol(starUUID, comUUID, solComponentInfo.getIpAddress(), solComponentInfo.getTcpPort(), com_comUUID);
            String responseJson = writeValueAsString(response);
            byte[] responseData = responseJson.getBytes();

            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
            udpSocket.send(responsePacket);
            comUUIDQueue.add(com_comUUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getComponentCount(){
        return components.size();
    }

    public boolean freeSpace(){
        return getComponentCount() < maxComponents;
    }

    public boolean queueContainsComUUID(String comUUID){
        return comUUIDQueue.contains(comUUID);
    }

    public void addComponent(ComponentInfo componentInfo){
        if(!components.containsKey(componentInfo.getComUUID()) && queueContainsComUUID(componentInfo.getComUUID())){
            components.put(componentInfo.getComUUID(), componentInfo);
            removeRegisterComUUID(componentInfo.getComUUID());
        } else {
            throw new IllegalArgumentException(String.format("Component %s already exists or %s never send by Sol", componentInfo.getComUUID(), componentInfo.getComUUID()));
        }
    }

    public ComponentInfo getComponentInfo(String comUUID){
        return components.get(comUUID);
    }

    public boolean checkIfComponentExists(String comUUID){
        return components.containsKey(comUUID);
    }


    public void removeRegisterComUUID(String comUUID){
        if(queueContainsComUUID(comUUID)) {
            comUUIDQueue.remove(comUUID);
        } else {
            throw new NoSuchElementException(String.format("%s nicht in den Anfragen Register", comUUID));
        }
    }





    private String generateStar_UUID(){
        try {
            String input = solComponentInfo.getIpAddress() + getGroupId() + comUUID;
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
    private String generateCOM_UUID(){
        String comUUID;
        do {
            comUUID = UUIDGenerator.generateCOM_UUID();
        } while(components.containsKey(comUUID));
       return comUUID;
    }



}
