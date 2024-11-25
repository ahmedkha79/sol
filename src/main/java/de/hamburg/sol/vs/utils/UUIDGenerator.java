package de.hamburg.sol.vs.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class UUIDGenerator {

    private static Random RANDOM = new Random();


    public static String generateCOM_UUID() {
        return String.valueOf(RANDOM.nextInt(9000) + 1000);
    }


}
