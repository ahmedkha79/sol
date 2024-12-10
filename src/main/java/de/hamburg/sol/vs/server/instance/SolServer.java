package de.hamburg.sol.vs.server.instance;

import de.hamburg.sol.vs.client.model.factory.SolComponentFactory;
import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.model.ComponentInfo;
import de.hamburg.sol.vs.utils.UUIDGenerator;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import java.util.NoSuchElementException;
import java.util.concurrent.*;

import static de.hamburg.sol.vs.utils.ProtocolHandler.*;

//Singleton
@Log4j2
@Lazy
public class SolServer implements Runnable {

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
    private ConcurrentHashMap<String, ComponentInfo> inactiveComponents = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ComponentInfo> activeComponents = new ConcurrentHashMap<>();
    //Registrierungsmap
    private ConcurrentLinkedQueue<String> comUUIDQueue = new ConcurrentLinkedQueue<>();

    private final ScheduledExecutorService scheduler;

    private volatile boolean running;

    private RestTemplate restTemplate;


    public SolServer(ScheduledExecutorService scheduler, int maxComponents, RestTemplate restTemplate) throws IllegalAccessException, SocketException {
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
        //putComponent(solComponentInfo);
        this.running = false;
        this.restTemplate = restTemplate;
    }


    @Override
    public void run() {


        this.running = true;
        log.info("Solserver wird gestartet mit starUUID: {}", starUUID);

        initializeSolComponent();


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

    private void terminateServer(){
        log.info("Sol wird heruntergefahren");
        stopServer();
        System.exit(0);
    }

    @PostConstruct
    private void postConstruct() {
        //initializeSolComponent();
        startComponentTimeOut(solComponentInfo);
    }


    private void initializeSolComponent() {
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
        return activeComponents.size();
    }

    public boolean freeSpace() {
        return getComponentCount() < maxComponents;
    }

    public boolean queueContainsComUUID(String comUUID) {
        return comUUIDQueue.contains(comUUID);
    }

    public void addComponent(ComponentInfo componentInfo) {
        if ((!activeComponents.containsKey(componentInfo.getComUUID()) && queueContainsComUUID(componentInfo.getComUUID()))) {
            putComponent(componentInfo);
            removeRegisterComUUID(componentInfo.getComUUID());
        } else {
            log.error("comUUID {} already exists or never send by Sol", componentInfo.getComUUID());
            throw new IllegalArgumentException(String.format("Component %s already exists or %s never send by Sol", componentInfo.getComUUID(), componentInfo.getComUUID()));

        }
    }

    private void putComponent(ComponentInfo componentInfo) {
        startComponentTimeOut(componentInfo);
        activeComponents.put(componentInfo.getComUUID(), componentInfo);
    }

    private void startComponentTimeOut(ComponentInfo componentInfo) {
        componentInfo.startTimeout(60, TimeUnit.SECONDS, () -> handleTimeout(componentInfo));
    }

    public synchronized void updateComponentLastSeen(String comUUID) {
        log.info("UPDATE");
        //ist es Sol selbst?
        if (comUUID.equals(this.comUUID)) {
            updateComponent(solComponentInfo);
            log.info("Sol wird geupdatet");
        } else if (activeComponents.containsKey(comUUID)) {
            ComponentInfo componentInfo = activeComponents.get(comUUID);
            updateComponent(componentInfo);
            log.info("Komponente wird geupdatet");
        } else {
            log.warn("Lebenszeichen von unbekannter Komponente {}", comUUID);
        }
    }

    private void updateComponent(ComponentInfo componentInfo) {
        componentInfo.updateLastInteraction();
        log.info("Komponente {} hat sich zurückgemeldet", comUUID);
        log.info("test");
        componentInfo.resetTimeout(60, TimeUnit.SECONDS);
        log.info("Timer zurück gesetzt");
    }

    private void handleTimeout(ComponentInfo componentInfo) {
        log.warn("Komponente {} reagiert nicht mehr. Sende Ping...", componentInfo.getComUUID());
        sendPingRequest(componentInfo);

    }


    protected void sendPingRequest(ComponentInfo componentInfo) {
        log.info("TEST");
        String url = String.format("http://%s:%d/vs/v1/system/%s?star=%s",
                componentInfo.getIpAddress(),
                componentInfo.getTcpPort(),
                componentInfo.getComUUID(),
                starUUID
        );
        ResponseEntity<String> getRequest = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        log.info("Ping - Request wurde gesendet");
        if (getRequest.getStatusCode().is2xxSuccessful()) {
            componentInfo.setStatus(getRequest.getStatusCode().toString());
            log.info("Komponente {} hat sich erfolgreich zurückgemeldet", componentInfo.getComUUID());
            updateComponentLastSeen(componentInfo.getComUUID());
        } else {

            componentInfo.setStatus("disconnected");
            moveFromActiveToInactive(componentInfo.getComUUID());
            componentInfo.updateLastInteraction();
        }

    }

    public void handleExitCommand() {
        log.info("EXIT - Befehl erhalten, schalte alle aktiven Komponenten ab");
        activeComponents.values().forEach(componentInfo -> {
            sendDeleteRequestToComponent(componentInfo);
            log.info("Komponente {} wird aus dem Stern abgeschaltet", componentInfo.getComUUID());
            componentInfo.setStatus("disconnected");
            moveFromActiveToInactive(componentInfo.getComUUID());
        });

        log.info("Alle Komponenten wurden kontaktiert. Beende SOL.");
        terminateServer();



    }

    public boolean sendDeleteRequestToComponent(ComponentInfo componentInfo) {
        String url = String.format("http://%s:%d/vs/v1/system/%s?star=%s",
                componentInfo.getIpAddress(),
                componentInfo.getTcpPort(),
                componentInfo.getComUUID(),
                starUUID
        );
        int timeout = 10000;
        boolean deleteSuccessful = false;
        int retries = 0;
        while(retries < 2 && !deleteSuccessful) {
            try {

                ResponseEntity<String> deleteRequest = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
                if (deleteRequest.getStatusCode().is2xxSuccessful()) {
                    deleteSuccessful = true;
                } else {
                    log.info("Komponente {} konnte nicht erreicht werden, erneut versuchen", componentInfo.getComUUID());
                    retries++;
                    waitBeforeRetry(timeout);
                }

            } catch (Exception e){
                log.error("Fehler beim Versenden, des DELETE Request");
            }
        }
        return deleteSuccessful;

    }

    private void waitBeforeRetry(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("Fehler beim Warten: {}", e.getMessage());
        }
    }

    private void moveFromActiveToInactive(String comUUID) {
        try {
           ComponentInfo componentInfo = activeComponents.remove(comUUID);
           addComponentToInactiveComponents(componentInfo);
        } catch (NoSuchElementException e) {
            log.error("Komponente mit {} nicht vorhanden ", comUUID);
            e.printStackTrace();
        }
    }

    private void addComponentToInactiveComponents(ComponentInfo componentInfo) {
        inactiveComponents.put(componentInfo.getComUUID(), componentInfo);
    }

    public ComponentInfo getComponentInfo(String comUUID) {
        return activeComponents.get(comUUID);
    }

    public boolean checkIfComponentExists(String comUUID) {
        return activeComponents.containsKey(comUUID);
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
        } while (activeComponents.containsKey(comUUID));
        return comUUID;
    }

    public SolProtocol getSolInfo() {
        return SolProtocol.builder()
                .star(starUUID)
                .sol(comUUID)
                .comUUID(comUUID)
                .ipAddress(starIpAddress)
                .port(starPort)
                .build();
    }


}
