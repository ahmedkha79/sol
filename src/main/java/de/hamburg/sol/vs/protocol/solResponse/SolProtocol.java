package de.hamburg.sol.vs.protocol.solResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolProtocol {

    //STAR_UUID
    @JsonProperty("star")
    private String star;
    @JsonProperty("sol")
    //COM_UUID von Sol
    private String sol;
    //IP-Adresse
    @JsonProperty("ip-Address")
    private String ipAddress;
    //STAR_PORT || COM-UUID
    @JsonProperty("port")
    private int port;
    //COM_UUID der neuen Komponente
    @JsonProperty("component")
    private String comUUID;
    @Nullable
    private String status;



    public SolProtocol(String star, String sol, String ipAddress, int port, String comUUID) {
        this.star = star;
        this.sol = sol;
        this.ipAddress = ipAddress;
        this.port = port;
        this.comUUID = comUUID;
    }

    public SolProtocol(String star, String sol, String ipAddress, int port, String comUUID, String status) {
        this.star = star;
        this.sol = sol;
        this.ipAddress = ipAddress;
        this.port = port;
        this.comUUID = comUUID;
        this.status = status;
    }




//    @Override
//    public String toString(){
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format(   "\"star\":%s \n" +
//                                "\"sol\":%s \n" +
//                                "\"ip\":%s \n" +
//                                "\"port\":%d \n" +
//                                "\"component\":%s \n"
//                                ,star, sol, ipAddress, port, comUUID));
//
//        if (status != null) {
//            sb.append(String.format("\"status\": %s \n", status));
//        }
//        return sb.toString();
//    }
}
