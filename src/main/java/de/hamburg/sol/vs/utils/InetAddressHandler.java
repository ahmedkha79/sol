package de.hamburg.sol.vs.utils;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class InetAddressHandler {


    private static String hostIp;

    static  {
        try {
            hostIp = readHostEnvironmentVariable();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getLocalHostAddress() {
      return hostIp;
    }

    private static String readHostEnvironmentVariable() throws SocketException {
        String value = System.getenv("HOST_IP");
        if(value != null){
           return value;
        } else {
            return getHostIP();
        }
    }


    public static String getHostIP() throws SocketException {
        // Liste aller Netzwerkschnittstellen des Hosts abrufen
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        // Alle Netzwerkschnittstellen durchgehen
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            // Nur die nicht-loopback (physische) Netzwerkschnittstellen betrachten
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            // Alle IP-Adressen dieser Schnittstelle durchgehen
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();

                // Wir suchen nach der ersten IPv4-Adresse, die nicht die Loopback-Adresse ist
                if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                    System.out.println(inetAddress.getHostAddress());
                    return inetAddress.getHostAddress();
                }
            }
        }
        return null;
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
