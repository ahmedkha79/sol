package de.hamburg.sol.vs.config.global;

import lombok.extern.log4j.Log4j2;


/**
 * Zentrale Konfigurationsklasse, um Ports zu setzen.
 * Umgebungsvariablen werden ausgelesen, ist dieser nicht gesetzt, wird ein Default-Port gewählt.
 */
@Log4j2
public class GlobalConfig {

    private static Integer STARPORT;
    private static Integer GROUP_ID;
    private static Integer GALAXY_PORT;
    private static Integer GALAXY_ID;

    private static String starUUID;

    private static final Integer DEFAULT_PORT = 8000;
    private static final int DEFAULT_GROUP_ID = 138;
    private static final int DEFAULT_GALAXY_ID = 200;




    static {
        GROUP_ID = readEnvironmentVariable("APP_GROUP_ID", DEFAULT_GROUP_ID);
        STARPORT = readEnvironmentVariable("APP_PORT", DEFAULT_PORT) + GROUP_ID;
        GALAXY_ID = readEnvironmentVariable("APP_GALAXY_ID", DEFAULT_GALAXY_ID);
        GALAXY_PORT = readEnvironmentVariable("APP_GALAXY_PORT", DEFAULT_PORT) + GALAXY_ID;  ;

    }

    private static int readEnvironmentVariable(String envName, int defaultValue) {
        String value = System.getenv(envName);
        System.out.println("Environment variable " + envName + " is " + value);

        if(value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Ungültiger Wert für " + envName + ": " + value + ". Verwende Standardport.");
            }
        }
        return defaultValue;
    }




    private static int setStarPort(Integer port) {
        if(STARPORT == null) {
            STARPORT = port + DEFAULT_GROUP_ID;
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

    public static int getGalaxyPort(){
        return GALAXY_PORT;
    }

    public static String getStarUUID() throws IllegalAccessException {
        if(starUUID == null) {
            throw new IllegalAccessException("Sol wurde nicht gestartet... ");
        } else {
            return starUUID;
        }
    }

    public static String setStarUUID(String starUUID) {
       if(GlobalConfig.starUUID == null) {
           GlobalConfig.starUUID = starUUID;
           log.info("starUUID: {} wurde gesetzt", starUUID);
       }

       return GlobalConfig.starUUID;

    }

}
