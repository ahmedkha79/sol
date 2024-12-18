package de.hamburg.sol.vs.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InetAddressHandler {


    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null; //Eventuell Default-IpAdresse
        }
    }



    public static boolean isIpReachable(String ip, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            return true; // Verbindung erfolgreich
        } catch (IOException e) {
            return false; // Verbindung fehlgeschlagen
        }
    }

}
