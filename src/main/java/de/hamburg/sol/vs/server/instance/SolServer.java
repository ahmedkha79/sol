package de.hamburg.sol.vs.server.instance;

import de.hamburg.sol.vs.client.model.factory.SolComponentFactory;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.UUIDGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
import java.util.concurrent.*;

import static de.hamburg.sol.vs.utils.ProtocolHandler.*;

//Singleton
@Log4j2
@Lazy
public class SolServer implements Runnable {
    //private static SolServer instance;

    @Autowired
    private ApplicationContext applicationContext;
    @Getter
    private final String comUUID;
    private LocalDateTime initializationTime;
    private int maxComponents;
    @Getter
    private ComponentInfo solComponentInfo;
    @Getter
    private SolComponent solComponent;
    @Getter
    private final String starUUID;
    @Setter
    private DatagramSocket udpSocket;

    private String starIpAddress;
    private int starPort;
    //Thread-safe
    private ConcurrentHashMap<String, ComponentInfo> components = new ConcurrentHashMap<>();
    //Registrierungsmap
    private ConcurrentLinkedQueue<String> comUUIDQueue = new ConcurrentLinkedQueue<>();

    private final ScheduledExecutorService scheduler;

    private volatile boolean running;


    public SolServer(ScheduledExecutorService scheduler, int maxComponents) throws IllegalAccessException, SocketException {
        this.scheduler = scheduler;
        this.comUUID = generateCOM_UUID();
        this.initializationTime = LocalDateTime.now();
        this.maxComponents = maxComponents;
        this.starIpAddress = getLocalHostAddress();
        this.starPort = getStarPort();
        this.solComponentInfo = new ComponentInfo(comUUID, getLocalHostAddress(), getStarPort());
        this.solComponentInfo.setStatus("200");
        this.starUUID = generateStar_UUID();
        this.udpSocket = new DatagramSocket(getStarPort());
        putComponent(solComponentInfo);
        this.running = false;
        initializeSolComponent();
    }


    @Override
    public void run() {


        this.running = true;
        log.info("Solserver wird gestartet");


        log.info("Sol lauscht auf Broadcast am Port: {}", starPort);

        while (running) {
            listenForBroadcastsRequests();
        }
    }

    public void stopServer() {
        this.running = false;
        Thread.currentThread().interrupt();
        if (udpSocket != null) {
            udpSocket.close();
        }
    }

    private void initializeSolComponent(){
        SolComponentFactory solComponentFactory = new SolComponentFactory(applicationContext);
        this.solComponent = solComponentFactory.createSolComponent(starUUID, comUUID, starIpAddress, starPort, comUUID, starIpAddress,
                starPort, false);

    }

    public void listenForBroadcastsRequests() {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(packet);

            String request = new String(packet.getData(), 0, packet.getLength());

            log.info("Empfangene Nachricht: {}", request);
            if (request.equals("HELLO?")) {
                respondToHello(packet.getAddress(), packet.getPort());
            } else {
                log.warn("Unknown request: {}", request);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Async
    protected void respondToHello(InetAddress address, int port) {
        try {
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

    public int getComponentCount() {
        return components.size();
    }

    public boolean freeSpace() {
        return getComponentCount() < maxComponents;
    }

    public boolean queueContainsComUUID(String comUUID) {
        return comUUIDQueue.contains(comUUID);
    }

    public void addComponent(ComponentInfo componentInfo) {
        if ((!components.containsKey(componentInfo.getComUUID()) && queueContainsComUUID(componentInfo.getComUUID()))) {
            putComponent(componentInfo);
            removeRegisterComUUID(componentInfo.getComUUID());
        } else {
            log.error("comUUID {} already exists or never send by Sol", componentInfo.getComUUID());
            throw new IllegalArgumentException(String.format("Component %s already exists or %s never send by Sol", componentInfo.getComUUID(), componentInfo.getComUUID()));

        }
    }

    private void putComponent(ComponentInfo componentInfo) {
        componentInfo.startTimeout(60, TimeUnit.SECONDS, () -> handleTimeout(componentInfo));
        components.put(componentInfo.getComUUID(), componentInfo);
    }

    public void updateComponentLastSeen(String comUUID) {
        ComponentInfo componentInfo = components.get(comUUID);
        if (componentInfo != null) {
            componentInfo.updateLastInteraction();
            //componentInfo.resetTimeout(60, TimeUnit.SECONDS);
            log.info("Komponente {} hat sich zurückgemeldet, Timer zurückgesetzt", comUUID);
        } else {
            log.warn("Lebenszeichen von unbekannter Komponente {}", comUUID);
        }
    }

    private void handleTimeout(ComponentInfo componentInfo){
        log.warn("Komponente {} reagiert nicht mehr. Sende Ping...", componentInfo.getComUUID());
        sendPingRequest(componentInfo);

    }

    @Async
    protected void sendPingRequest(ComponentInfo componentInfo){

    }

    public ComponentInfo getComponentInfo(String comUUID) {
        return components.get(comUUID);
    }

    public boolean checkIfComponentExists(String comUUID) {
        return components.containsKey(comUUID);
    }


    public void removeRegisterComUUID(String comUUID) {
        if (queueContainsComUUID(comUUID)) {
            comUUIDQueue.remove(comUUID);
        } else {
            log.error("comUUID {} does not exist", comUUID);
            throw new NoSuchElementException(String.format("%s nicht in den Anfragen Register", comUUID));
        }
    }


    private String generateStar_UUID() {
        try {
            String input = solComponentInfo.getIpAddress() + getGroupId() + comUUID;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : hashBytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String generateCOM_UUID() {
        String comUUID;
        do {
            comUUID = UUIDGenerator.generateCOM_UUID();
        } while (components.containsKey(comUUID));
        return comUUID;
    }

    public SolProtocol getSolInfo(){
        return SolProtocol.builder()
                .star(starUUID)
                .sol(comUUID)
                .comUUID(comUUID)
                .ipAddress(starIpAddress)
                .port(starPort)
                .build();
    }


}
