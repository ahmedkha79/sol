package de.hamburg.sol.vs.messages.datatype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageList {
    @JsonProperty("star")
    private String star;
    @JsonProperty("totalResults")
    private int totalResults;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("view")
    private String view;
    @JsonProperty("messages")
    private List<Message> messages;
}
