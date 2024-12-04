package de.hamburg.sol.vs.server.instance;

import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.UUIDGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static de.hamburg.sol.vs.config.GlobalConfig.*;
import static de.hamburg.sol.vs.utils.InetAddressHandler.*;

import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import static de.hamburg.sol.vs.utils.ProtocolHandler.*;

//Singleton
@Log4j2
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
    private int starPort;
    //Thread-safe
    private ConcurrentHashMap<String, ComponentInfo> components = new ConcurrentHashMap<>();
    //Registrierungsmap
    private ConcurrentLinkedQueue<String> comUUIDQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean running;

    @Autowired
    private RestTemplate restTemplate;

    public SolServer(int maxComponents) throws IllegalAccessException, SocketException {
        this.comUUID = generateCOM_UUID();
        this.initializationTime = LocalDateTime.now();
        this.activeComponents = 1;
        this.maxComponents = maxComponents;
        this.starPort = getStarPort();
        this.solComponentInfo = new ComponentInfo(comUUID, getLocalHostAddress(), getStarPort());
        this.solComponentInfo.setStatus("200");
        this.starUUID = generateStar_UUID();
        this.udpSocket = new DatagramSocket(getStarPort());
        components.put(comUUID, solComponentInfo);
        this.running = false;
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
        if (!components.containsKey(componentInfo.getComUUID()) && queueContainsComUUID(componentInfo.getComUUID())) {
            components.put(componentInfo.getComUUID(), componentInfo);
            removeRegisterComUUID(componentInfo.getComUUID());
        } else {
            log.error("comUUID {} already exists or never send by Sol", componentInfo.getComUUID());
            throw new IllegalArgumentException(String.format("Component %s already exists or %s never send by Sol", componentInfo.getComUUID(), componentInfo.getComUUID()));

        }
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

    /**
     * SOL prüft aktiv den Status der Komponente
     */
    public void verifyComponentStatus(String comUUID) {
        ComponentInfo component = components.get(comUUID);

        if (component == null) {
            log.warn("Komponente mit UUID {} exisitert nicht.", comUUID);
            return;
        }

        String url = String.format("http://%s:%d/vs/v1/system/%s?star=%s",
                component.getIpAddress(),
                component.getTcpPort(),
                comUUID,
                starUUID);

        try {
            log.info("Überprüfe Status der Komponente {} über UNICAST: {}", comUUID, url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Komponente {} ist aktiv.", comUUID);
                component.setLastInteraction(LocalDateTime.now());
                component.setStatus("200");
            } else {
                log.warn("Komponente {} hat einen unerwarteten Status: {}", comUUID, response.getStatusCode());
                markComponentAsDisconnected(comUUID);
            }
        } catch (Exception e) {
            log.error("Fehler beim Überprüfend er Komponente {}: {}", comUUID, e.getMessage());
            markComponentAsDisconnected(comUUID);
        }
    }

    private void markComponentAsDisconnected(String comUUID) {
        ComponentInfo component = components.get(comUUID);
        if (component != null) {
            component.setStatus("disconnected");
            log.warn("Komponente {} wurde als 'disconnected' markiert.", comUUID);
        }
    }

    /**
     * Regelmäßige Überprüfung der Komponente, die keinen Heartbeat senden
     */
    @Scheduled(fixedRate = 60000)
    public void checkAndVerifyActiveComponents(){
        components.values().forEach(component -> {
            if (component.getLastInteraction().isBefore(LocalDateTime.now().minusSeconds(60))) {
                log.warn("Kein Hearbeat von Komponente seit 60 Sekunden. Starte UNICAST-Überprüfung.", component.getComUUID());
                verifyComponentStatus(component.getComUUID());
            }
        });
    }

    /**
     * Abmelden von SOL
     */
    public void exitAndShutdown() {
        log.info("EXIT-BEFEHL erhalten. Beginne mit dem Entfernen aller aktiven Komponenten...");

        components.values().forEach(component -> {
            if (!component.getComUUID().equals(this.comUUID)) {
                boolean success = tryToRemoveComponent(component);
                if (!success) {
                    log.warn("Komponente {} konnte nicht erreicht werden. Markeire als 'disconnected'.", component.getComUUID());
                    component.setStatus("disconnected");
                }
            }
        });

        log.info("Alle Komponenten wurden kontaktiert. Beende SOL.");
        stopServer();
    }

    private boolean tryToRemoveComponent(ComponentInfo component) {
        String url = String.format("http://%s:%d/vs/v1/system/%s?star=%s",
                component.getIpAddress(),
                component.getTcpPort(),
                component.getComUUID(),
                this.starUUID);

        for (int i = 0; i < 3; i++) {
            try {
                log.info("Sende DELETE-Befehl an Komponente {}. Versuch {}.", component.getComUUID(), i + 1);
                restTemplate.delete(url);
                log.info("Komponente {} hat den DELETE-Befehl akzeptiert.", component.getComUUID());
                return true;
            } catch (Exception e) {
                log.error("Fehler beim Kontaktieren der Komponente {}: {}", component.getComUUID(), e.getMessage());
                waitBeforRetry((i+1)*10000); // 10 Sekunden beim ersten Fehler, 20 Sekunden beim zweiten
            }
        }
        return false;
    }

    private void waitBeforRetry(int miliSeconds){
        try {
            Thread.sleep(miliSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Warten wurde unterbrochen.");
        }
    }
}
