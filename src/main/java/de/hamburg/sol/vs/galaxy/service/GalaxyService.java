package de.hamburg.sol.vs.galaxy.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.hamburg.sol.vs.config.global.GlobalConfig;
import de.hamburg.sol.vs.galaxy.datatype.StarInfo;
import de.hamburg.sol.vs.galaxy.model.GalaxyModel;
import de.hamburg.sol.vs.server.config.SolServerConfig;
import de.hamburg.sol.vs.server.instance.SolServer;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hamburg.sol.vs.config.global.GlobalConfig.getStarUUID;
import static de.hamburg.sol.vs.utils.ProtocolHandler.convertJsonToObject;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

@Service
@Log4j2
@Lazy
public class GalaxyService {


    private final SolServerConfig solServerConfig;
    private SolServer solServer;

    private final boolean running;

    private final RestTemplate restTemplate;

    private final GalaxyModel galaxyModel;

    private final String solStarUUID;

    private final int galaxyPort = GlobalConfig.getGalaxyPort();

    private final String regex = "^HELLO\\? I AM (.+)$";

    private final Pattern starUUIDPattern = Pattern.compile(regex);

    public GalaxyService(@Lazy SolServer solServer, RestTemplate restTemplate, GalaxyModel galaxyModel, SolServerConfig solServerConfig) throws IllegalAccessException {
        this.solServer = solServer;
        this.restTemplate = restTemplate;
        this.galaxyModel = galaxyModel;
        this.solStarUUID = getStarUUID();
        this.running = true;
        this.solServerConfig = solServerConfig;
    }

    @PostConstruct
    public void start(){

            Thread galaxyListener = new Thread(this::listenForGalaxyBroadcast);
            log.info("Starting Galaxy Listener");
            galaxyListener.start();

    }

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void sendGalaxyBroadcast(){
        log.info("Sending Galaxy Broadcast...");
        sendHelloGalaxyRequest();



    }


    public void listenForGalaxyBroadcast(){
        try(DatagramSocket socket = new DatagramSocket(galaxyPort)){
            log.info("Listening for Galaxy Broadcast");
            while(running) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String galaxyRequest = new String(packet.getData(), 0, packet.getLength());

                log.info("Galaxy request: " + galaxyRequest);
                String starUUID = extractStarUUID(galaxyRequest);

                InetAddress address = packet.getAddress();

                handleGalaxyRequest(starUUID, address);
            }
        } catch (IOException | IllegalArgumentException e) {
            log.error(e);
        }
        log.info("Beende den Galaxy-Listener");
    }

    private void handleGalaxyRequest(String starUUID, InetAddress address) throws JsonProcessingException {

        try {
            if (!galaxyModel.containsStar(starUUID)) {
                log.info("Sende Registrierungsanfrage an folgenden Stern: {}", starUUID);
                ResponseEntity<String> response = handleRequest(address, POST);

                if (response != null && response.getStatusCode().is2xxSuccessful()) {
                    log.info("Stern konnte sich erfolgreich bei: {} registrieren ", starUUID);
                    StarInfo starInfo = convertJsonToObject(response.getBody(), StarInfo.class);
                    galaxyModel.putStarIntoMap(starInfo);
                }


            } else {

                log.info("Sende Galaxy-Patch an folgenden Stern: {}", starUUID);
                StarInfo starInfo = getStarInfo(starUUID);

                if (checkIpAddress(starInfo, address) && checkStatus(starInfo)) {
                    log.info("Status des Stern: {}", starInfo.getStatus());
                    //PATCH - Request
                    if (handleRequest(address, PATCH).getStatusCode().is2xxSuccessful()) {
                        log.info("Erfolgreich bei Stern: {} zurückgemeldet", starUUID);
                    }
                } else {
                    starInfo.setStatus("disallowed");
                }
            }
        } catch (RestClientException e) {
            log.error("Fehler beim Senden des Requests ");
            log.error(e);
        }
    }

    private boolean checkStatus(StarInfo starInfo){
            boolean value = starInfo.getStatus().equals("200");
            if(!value) log.info("Status der Komponente ist nicht 200, sondern: {}", starInfo.getStatus());
            return value;
    }



    private ResponseEntity<String> handleRequest(InetAddress address, HttpMethod method){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.TEXT_PLAIN));


        StarInfo solInfo = solServer.updateAndGetStarInfo();

        HttpEntity<StarInfo> entity = new HttpEntity<>(solInfo, headers);

        return sendRequest(address, entity, method);


    }

    private ResponseEntity<String> sendRequest(InetAddress address, HttpEntity<StarInfo> entity, HttpMethod method){
        return switch (method.name()){
            case "POST" -> postRequestToStar(address, entity);
            case "PATCH" -> patchRequestExistingStar(address, entity);
            default -> throw new IllegalStateException("Unexpected value: " + method.name());
        };
    }

    private ResponseEntity<String> postRequestToStar(InetAddress address, HttpEntity<StarInfo> entity){
        String url = String.format("http://%s:%d/vs/v1/star", address.getHostAddress(), galaxyPort);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response;
    }

    private ResponseEntity<String> patchRequestExistingStar(InetAddress address, HttpEntity<StarInfo> entity){
        String url = String.format("http://%s:%d/vs/v1/star/%s",address.getHostAddress(), galaxyPort, solStarUUID);
        ResponseEntity<String> response = restTemplate.exchange(url, PATCH, entity, String.class);

        return response;
    }

    private void sendHelloGalaxyRequest(){
        byte[] message = String.format("HELLO? I AM %s", solStarUUID).getBytes();
        InetSocketAddress broadCastAddress = new InetSocketAddress("255.255.255.255", galaxyPort);
        DatagramPacket helloPacket = new DatagramPacket(message, message.length, broadCastAddress);

        try(DatagramSocket socket = new DatagramSocket()){
            socket.send(helloPacket);
            log.info("Galaxy-Request gesendet");
        } catch(IOException e){
            log.error(e);
        }

    }

    private boolean checkIpAddress(StarInfo starInfo, InetAddress ipAddress){
        boolean value = starInfo != null && starInfo.getIpAddress().equals(ipAddress.getHostAddress());
        if(!value){
            log.error("IP-Adresse: {} nicht gleich mit der erhaltenen IP-Adresse: {}", ipAddress.getHostAddress(), starInfo.getIpAddress());
        }
        return value;
    }

    private StarInfo getStarInfo(String starUUID){
        return galaxyModel.getStarInfo(starUUID);
    }


    private String extractStarUUID(String message){
        Matcher matcher = starUUIDPattern.matcher(message);

        if(matcher.matches()){
            return matcher.group(1);
        }
        throw new IllegalArgumentException("StarUUID konnte nicht aus der Nachricht: " + message + "extrahiert werden");
    }
}
