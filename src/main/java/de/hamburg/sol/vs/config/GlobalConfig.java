package de.hamburg.sol.vs.config;

public class GlobalConfig {

    private static final int DEFAULT_STARPORT = 8000;
    private static final int GROUP_ID = 6;



    public static int getStar_Port(){
        return DEFAULT_STARPORT + GROUP_ID;
    }

    public static int getSpecificStar_Port(int port){
        return port + GROUP_ID;
    }


}
