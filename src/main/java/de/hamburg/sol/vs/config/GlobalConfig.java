package de.hamburg.sol.vs.config;

public class GlobalConfig {

    private static Integer STARPORT;
    private static final Integer DEFAULT_PORT = 8000;
    private static final int GROUP_ID = 6;

    static {
        String portFromEnv = System.getenv("APP_PORT");
        if(portFromEnv != null) {
            try{
                setStarPort(Integer.parseInt(portFromEnv));
            } catch(NumberFormatException e) {
                System.err.println("Ungültiger Wert für APP_PORT: " + portFromEnv + ". Verwende Standardport.");
                setStarPort(DEFAULT_PORT);
                System.err.println("Verwende Standardport.");
            }
        } else {
            setStarPort(DEFAULT_PORT);
        }
    }


    private static int setStarPort(Integer port) {
        if(STARPORT == null) {
            STARPORT = port + GROUP_ID;
        }

        return STARPORT;
    }

    public static int getStarPort() throws IllegalAccessException {
        if (STARPORT == null) {
            throw new IllegalAccessException("STARPORT-Variable ist nicht gesetzt.");
        }
        return STARPORT;
    }

    public static int getGroupId(){
        return GROUP_ID;
    }



}
