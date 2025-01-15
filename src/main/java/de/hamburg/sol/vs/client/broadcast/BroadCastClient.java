package de.hamburg.sol.vs.client.broadcast;



import de.hamburg.sol.vs.client.model.instance.SolComponent;
import de.hamburg.sol.vs.client.model.factory.SolComponentFactory;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.server.instance.SolServer;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static de.hamburg.sol.vs.config.global.GlobalConfig.getStarPort;
import static de.hamburg.sol.vs.utils.InetAddressHandler.getLocalHostAddress;
import static de.hamburg.sol.vs.utils.ProtocolHandler.convertJsonToObject;
@Component
@Scope("prototype")
@Log4j2
public class BroadCastClient implements Runnable {


    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RestTemplate restTemplate;

    private final Integer STAR_PORT;

    @Autowired
    private Environment environment;

    @Setter
    private DatagramSocket udpSocket;

    private String ipAddress = getLocalHostAddress();
//    //TCP-Port
//    private ServerSocket serverSocket = new ServerSocket(0);

    private int port;
    private final int REQUEST_Timeout = 20;
    private final int CHECK_RETRIES_INTERVAL = 2;

    private volatile boolean isBroadcasting;

    private ConcurrentLinkedQueue<String> responses;

    public BroadCastClient(Environment environment) throws IllegalAccessException, IOException {
        STAR_PORT = getStarPort();
        responses = new ConcurrentLinkedQueue<>();
        this.udpSocket = new DatagramSocket();
        this.udpSocket.setBroadcast(true);
        this.environment = environment;
        port = getStarPort();
        //port = Integer.parseInt(environment.getProperty("server.port", "8080"));



    }

    @Override
    public void run() {
        discoverStar();
    }


    public void discoverStar(){
    new Thread(() -> {
        try{
            boolean firstRequest = true;
            isBroadcasting = true;
            boolean registered = false;
            log.info("Starte die Sternensuche...");
            int retries = 0;
            //Schleife die ein Stern sucht
            while(retries < CHECK_RETRIES_INTERVAL && isBroadcasting){
                log.info("Broadcast Nr. {}", retries + 1);
                 sendHelloRequest();

                 if(firstRequest){

                     new Thread(this::receiveResponses, "RespondListener-Thread").start();
                     log.debug("RespondListener-Thread started");
                     firstRequest = false;
                 }
                 //Timeout um auf Nachrichten zu hören
                 Thread.sleep(REQUEST_Timeout*1000L);
                 retries++;

                 synchronized (responses){
                     if(!responses.isEmpty()){
                       try {
                            for(String responseJson: responses){
                                SolProtocol solProtocol = convertJsonToObject(responseJson, SolProtocol.class);
                                log.info("Response von Star: {}", solProtocol.getStar());
                                log.info("Bearbeite Response: {}", responseJson);

                                registered = registerWithSol(solProtocol);
                                log.info("Registrierung an Star: {} geschickt", solProtocol.getStar());
                                if(registered){
                                    log.info("Erfolgreich im Star: {} registriert", solProtocol.getStar());
                                    log.info("Solkomponente starten... mit Port: {}", port);


                                    SolComponentFactory solComponentFactory = new SolComponentFactory(applicationContext);
                                    SolComponent solComponent = solComponentFactory.createSolComponent(solProtocol.getStar(),
                                            solProtocol.getSol(), solProtocol.getIpAddress(), solProtocol.getPort(), solProtocol.getComUUID(),
                                            ipAddress, port);

                                    log.info("Solkomponente mit folgenden Werten: {}", solComponent);



                                    //serverSocket.close();

                                    isBroadcasting = false;

                                    break;

                                }
                            }
                            responses.clear();
                       } catch (Exception e){
                            e.printStackTrace();
                           log.error("Probleme mit dem Auslesen von Antworten");
                       }

                     }
                 }
            }

            if(!registered){
                log.info("Kein Stern wurde gefunden");
                Thread solServerThread = new Thread(() -> {
                    SolServer solServer = applicationContext.getBean(SolServer.class);
                    //solServer.run();
                    solServer.start();
                });
                log.info("SolServer initialisieren");
                solServerThread.start();
                log.info("SolServer starten");

            }

        } catch (Exception e) {
            log.error("Probleme mit dem Versenden des Broadcasts");
            e.printStackTrace();
        }
    }, "DiscoverStar-Thread").start();

    }



    private void sendHelloRequest() throws IOException {
        byte[] message = "HELLO?".getBytes();
        InetSocketAddress broadCastAddress = new InetSocketAddress("255.255.255.255", STAR_PORT);
        DatagramPacket request = new DatagramPacket(message, message.length, broadCastAddress);
        udpSocket.send(request);
        log.debug("UDP-Datenpaket rausgesendet");

    }

    private void receiveResponses() {
            while(isBroadcasting){
                try{
                    byte[] buffer = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(responsePacket);
                    log.debug("Datenpaket erhalten");
                    String responseJson = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);

                    synchronized (responses) {
                        responses.add(responseJson);
                        log.debug("Response zur Queue hinzugefügt");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }



    private boolean registerWithSol(SolProtocol solProtocol){
        String url = String.format("http://%s:%d/vs/v1/system", solProtocol.getIpAddress(), solProtocol.getPort());
        try{
            int timeout = 5000;

            log.info("Erstelle Registrierungsanfrage mit {} Sekunden Timeout", timeout/1000);


            SolProtocol registerInfo = SolProtocol.builder()
                    .star(solProtocol.getStar())
                    .sol(solProtocol.getSol())
                    .comUUID(solProtocol.getComUUID())
                    .ipAddress(getLocalHostAddress())
                    .port(port)
                    .status("200")
                    .build();
            log.debug("Registrierungsanfrage erzeugt ");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.TEXT_PLAIN));


            HttpEntity<SolProtocol> entity = new HttpEntity<>(registerInfo, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);


            return response.getStatusCode().is2xxSuccessful();

        } catch (RestClientException e) {
            log.error("Fehler, Anfrage an folgende Adresse zu senden: {}", url);
            e.printStackTrace();
        }
        return false;
    }




}

