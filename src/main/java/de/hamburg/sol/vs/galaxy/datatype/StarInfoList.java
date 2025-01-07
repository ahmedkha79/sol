package de.hamburg.sol.vs.galaxy.datatype;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class StarInfoList {

    @JsonProperty("totalResults")
    private int totalResults;

    @JsonProperty("stars")
    private List<StarInfo> stars;
}
