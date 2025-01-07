package de.hamburg.sol.vs.galaxy.datatype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarInfo {

    //STAR_UUID
    @JsonProperty("star")
    private String star;

    //Sol-comUUID
    @JsonProperty("sol")
    private String sol;

    //Sol-IP-Adresse
    @JsonProperty("sol-ip")
    private String ipAddress;

    @JsonProperty("sol-tcp")
    private int port;

    @JsonProperty("no-com")
    private int number_of_components;

    @JsonProperty("status")
    private String status;
}
