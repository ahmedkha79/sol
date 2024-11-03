package de.hamburg.sol.vs.server.config;

public class GlobalConfig {

    private static final int DEFAULT_STARPORT = 8000;
    private static final int GROUP_ID = 6;



    public static int getStar_Port(){
        return DEFAULT_STARPORT + GROUP_ID;
    }

    public static int getStar_Port(int port){
        return port + GROUP_ID;
    }

    public static int getGroupId(){
        return GROUP_ID;
    }


}
