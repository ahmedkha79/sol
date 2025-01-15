package de.hamburg.sol.vs.galaxy.datatype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StarInfo starInfo)) return false;
        return port == starInfo.port && Objects.equals(star, starInfo.star) && Objects.equals(sol, starInfo.sol) && Objects.equals(ipAddress, starInfo.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(star, sol, ipAddress, port);
    }
}
