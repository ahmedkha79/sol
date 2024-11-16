package de.hamburg.sol.vs.client.broadcast;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static de.hamburg.sol.vs.server.config.GlobalConfig.getStarPort;

public class BroadcastClient {


    private final Integer STAR_PORT;

    private final int REQUEST_Timeout = 20;
    private final int CHECK_RETRIES_INTERVAL = 2;

    private List<String> responses;

    public BroadcastClient() throws IllegalAccessException {
        STAR_PORT = getStarPort();
        responses = new ArrayList<>();

    }


    public void discoverStar(){
    new Thread(() -> {
        try(DatagramSocket udpBroadCastSocket = new DatagramSocket()){
            udpBroadCastSocket.setBroadcast(true);
            int retries = 0;
            while(retries < CHECK_RETRIES_INTERVAL){
                 sendHelloRequest(udpBroadCastSocket);
                 Thread.sleep(REQUEST_Timeout*1000L);
                 retries++;

                 synchronized (responses){
                     if(!responses.isEmpty()){
                         //TODO SolResponse implementieren
                     }
                 }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }, "DiscoverStar-Thread").start();
    }


    private void sendHelloRequest(DatagramSocket socket) throws IOException {
        byte[] message = "HELLO?".getBytes();
        InetSocketAddress broadCastAddress = new InetSocketAddress("255.255.255.255", STAR_PORT);
        DatagramPacket request = new DatagramPacket(message, message.length, broadCastAddress);
        socket.send(request);


    }

    private void receiveResponses(DatagramSocket socket) {
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < (REQUEST_Timeout*1000)){
                try{
                    byte[] buffer = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.setSoTimeout(1000);

                    socket.receive(responsePacket);
                    synchronized (responses) {
                        responses.add(responsePacket.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
}

