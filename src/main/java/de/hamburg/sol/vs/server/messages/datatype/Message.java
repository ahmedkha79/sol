package de.hamburg.sol.vs.server.messages.datatype;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;
}
