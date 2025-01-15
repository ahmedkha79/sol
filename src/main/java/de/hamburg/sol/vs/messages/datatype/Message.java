package de.hamburg.sol.vs.messages.datatype;


import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class Message {

    @JsonProperty("star")
    private String star;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("sender")
    private String sender;

    @Nullable
    @JsonProperty("msg_id")
    private String msg_id;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("changed")
    private LocalDateTime changed;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("from-star")
    private String from_star;


    @JsonProperty("received")
    private LocalDateTime received;


    @JsonProperty("delivered")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DeliveredInfo> delivered;

    @JsonProperty("removed")
    private LocalDateTime removed;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;


    public void updateChanged(){
        this.changed=LocalDateTime.now();
    }

    public void updateReceived(){
        this.received=LocalDateTime.now();
    }


    public void updateRemoved(){
        this.removed=LocalDateTime.now();
    }

    @JsonIgnore
    public void putIntoDelivered(String toStar, LocalDateTime deliveredTime){
        if(delivered == null) delivered = new ArrayList<>();
        DeliveredInfo deliveredInfo = new DeliveredInfo(toStar, deliveredTime);
        delivered.add(deliveredInfo);

    }

    public List<DeliveredInfo> getDelivered(){
        if(delivered == null) return new ArrayList<>();
        return delivered;
    }

    @JsonIgnore
    public List<DeliveredInfo> getDeliveredStars(){
        if(delivered == null){
            return new ArrayList<>();
        }
        return delivered.stream().map(deliveredInfo -> new DeliveredInfo(deliveredInfo.getToStar(), null)).toList();
    }



}

@Data
@AllArgsConstructor
@Builder
 class DeliveredInfo {

    @JsonProperty("deliveredStar")
    private String toStar;

    @JsonProperty("deliveredTimeStamp")
    private LocalDateTime deliveredTimeStamp;
}

