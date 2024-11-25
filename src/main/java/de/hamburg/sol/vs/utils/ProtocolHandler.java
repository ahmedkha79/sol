package de.hamburg.sol.vs.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ProtocolHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static String writeValueAsString(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    public static <T> T convertJsonToObject(String value, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(value, valueType);
    }
}
