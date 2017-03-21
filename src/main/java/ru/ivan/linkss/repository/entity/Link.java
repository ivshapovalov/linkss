package ru.ivan.linkss.repository.entity;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Link implements JSONable{

    private String key;
    private String link;
    private IpPosition ipPosition;

    public Link() {

    }

    public Link(Builder builder) {
        this.key = builder.key;
        this.link = builder.link;
        this.ipPosition = builder.ipPosition;
    }

    public String getKey() {
        return key;
    }

    public String getLink() {
        return link;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLink(String link) {
        this.link = link;
    }


    @Override
    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);

    }

    public static Link fromJSON(String json) throws IOException {
        return new ObjectMapper().readValue(json,Link.class);
    }

    public IpPosition getIpPosition() {
        return ipPosition;
    }

    public void setIpPosition(IpPosition ipPosition) {
        this.ipPosition = ipPosition;
    }

    public static class Builder {
        private String key;
        private String link;
        private IpPosition ipPosition;

        public Builder() {
        }

        public Builder addKey(String key) {
            this.key = key;
            return this;
        }

        public Builder addLink(String link) {
            this.link = link;
            return this;
        }

        public Builder addIpPosition(IpPosition ipPosition) {
            this.ipPosition = ipPosition;
            return this;
        }

        public Link build() {
            return new Link(this);
        }
    }
}
