package de.hamburg.sol.vs.server.instance.response;

public class SolResponse {

    //STAR_UUID
    private String star;
    //COM_UUID von Sol
    private int sol;
    //IP-Adresse von Sol
    private String solIpAddress;
    //STAR_PORT
    private int solPort;
    //COM_UUID der neuen Komponente
    private int comUUID;


    public SolResponse(String star, int sol, String solIpAddress, int solPort, int comUUID) {
        this.star = star;
        this.sol = sol;
        this.solIpAddress = solIpAddress;
        this.solPort = solPort;
        this.comUUID = comUUID;
    }

    public int getComUUID() {
        return comUUID;
    }

    public void setComUUID(int comUUID) {
        this.comUUID = comUUID;
    }

    public int getSolPort() {
        return solPort;
    }

    public void setSolPort(int solPort) {
        this.solPort = solPort;
    }

    public String getSolIpAddress() {
        return solIpAddress;
    }

    public void setSolIpAddress(String solIpAddress) {
        this.solIpAddress = solIpAddress;
    }

    public int getSol() {
        return sol;
    }

    public void setSol(int sol) {
        this.sol = sol;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    @Override
    public String toString(){
        return String.format(   "\"star\":%s \n" +
                                "\"sol\":%d \n" +
                                "\"sol-ip\":%s \n" +
                                "\"sol-tcp\":%d \n" +
                                "\"component\":%d \n"
                                ,star, sol, solIpAddress, solPort, comUUID);
    }
}
