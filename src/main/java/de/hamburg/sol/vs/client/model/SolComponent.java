package de.hamburg.sol.vs.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;

@Component
@Scope("prototype")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SolComponent {
    private String starUUID;
    private String solUUID;
    private String solIpAddress;
    private int solPort;

    private String comUUID;
    private String comIpAddress;
    private int comPort;
    private ServerSocket tcpServerSocket;
    private final String restApiUrl = String.format("http://%s:%d/vs/v1/system", solIpAddress, solPort);


    public void startComponent(){
        try {
            tcpServerSocket = new ServerSocket(comPort);
        } catch(IOException e){
            e.printStackTrace();
            System.out.println("TCP Socket konnte nicht erzeugt werden");
        }

    }


}
