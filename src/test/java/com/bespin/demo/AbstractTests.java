package com.bespin.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTests {

    private final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    protected String toJson(final Object obj) {
        try {
            return JACKSON_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
