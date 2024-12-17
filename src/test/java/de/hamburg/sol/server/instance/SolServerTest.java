package de.hamburg.sol.server.instance;

import de.hamburg.sol.vs.server.instance.SolServer;
import de.hamburg.sol.vs.protocol.SolProtocol;
import de.hamburg.sol.vs.utils.ProtocolHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.net.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static de.hamburg.sol.vs.config.global.GlobalConfig.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Profile("test")
public class SolServerTest {

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private SolServer server;

    @MockBean
    @Lazy
    private DatagramSocket mockSocket;
    private String responseJSON;
    private byte[] data;



    @BeforeEach
    public void setUp() throws IllegalAccessException, SocketException {
        mockSocket = mock(DatagramSocket.class);
        server.setUdpSocket(mockSocket);

    }

    @Test
    public void testSingletonInstance() throws IllegalAccessException {
        try {
            SolServer instance1 = applicationContext.getBean(SolServer.class);
            SolServer instance2 = applicationContext.getBean(SolServer.class);
            assertSame(instance1, instance2);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

//    @Test
//    public void testListenForBroadcasts() {
//        try {
//
//            DatagramSocket datagramSocket = new DatagramSocket();
//            String msg = "HELLO?";
//            byte[] buf = msg.getBytes();
//            byte[] receiveBuffer = new byte[1024];
//            DatagramPacket packet = new DatagramPacket(buf, buf.length, new InetSocketAddress("255.255.255.255", getStarPort()));
//            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//            Thread serverThread = new Thread(server);
//            serverThread.start();
//            Thread.sleep(100);
//            datagramSocket.send(packet);
//            serverThread.join();
//            server.stopServer();
//            datagramSocket.receive(receivePacket);
//
//            assertTrue(receivePacket.getLength() > 0);
//            assertTrue(receivePacket.getPort() == packet.getPort());
//            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
//            System.out.println(receivedMessage);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testListenForBroadcasts_MOCK(){
        try {
            //Set up DatagramPacket
            data = "HELLO?".getBytes();

            mockUpReceiveMethod(data);
            startAndStopServer();

            //Fangt DatagramPacket ab
            ArgumentCaptor<DatagramPacket> packetCaptor = ArgumentCaptor.forClass(DatagramPacket.class);

            //Überprüfen, ob Mockmethode in listenForBroadcast aufgerufen
            verify(mockSocket, times(1)).send(packetCaptor.capture());

            //Abfangen der Antwort
            DatagramPacket packet = packetCaptor.getValue();

            String receivedMessage = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            System.out.println(receivedMessage);

            //SolResponse solResponse = objectMapper.readValue(receivedMessage, SolResponse.class);
            SolProtocol solProtocol = ProtocolHandler.convertJsonToObject(receivedMessage, SolProtocol.class);

            //Überprüft, ob die SolResponse

            assertTrue(packet.getPort() == getStarPort());
            assertEquals(solProtocol.getSol(), server.getComUUID());
            verify(mockSocket, atLeast(1)).receive(any(DatagramPacket.class));


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startAndStopServer() throws InterruptedException{
        Thread serverThread = new Thread(() -> {
                server.listenForBroadcastsRequests();

        });
        serverThread.start();
        Thread.sleep(200);
        server.stop();
        serverThread.join();
        System.out.println("Gestoppt");

    }

    private void mockUpReceiveMethod(byte[] data){
        System.out.println(data);
        //Mock up receive Method
        try {
            doAnswer(invocation -> {
                DatagramPacket receivePacket = (DatagramPacket) invocation.getArguments()[0];
                    System.arraycopy(data, 0, receivePacket.getData(), 0, data.length);
                    receivePacket.setLength(data.length);
                    receivePacket.setPort(getStarPort());
                    System.out.println("Daten empfangen (Mock): " + new String(data));
                return null;
            }).when(mockSocket).receive(any(DatagramPacket.class));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testListenForBroadcast_Fail(){
        data = "BYE?".getBytes();
        try {
            mockUpReceiveMethod(data);
            startAndStopServer();
            ArgumentCaptor<DatagramPacket> packetCaptor = ArgumentCaptor.forClass(DatagramPacket.class);
            //Überprüfen, ob Mockmethode in listenForBroadcast aufgerufen
            verify(mockSocket, never()).send(any(DatagramPacket.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
