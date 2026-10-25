package com.craftora.storefront;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class StorefrontConfigCodec {

    private final JsonMapper mapper;

    public StorefrontConfigCodec(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public String write(StorefrontConfig config) {
        return mapper.writeValueAsString(config);
    }

    public StorefrontConfig read(String json) {
        return json == null ? null : mapper.readValue(json, StorefrontConfig.class);
    }
}
