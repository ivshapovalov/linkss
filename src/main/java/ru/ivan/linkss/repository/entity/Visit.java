package ru.ivan.linkss.repository.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Visit  {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime time;

    private IpPosition ipPosition;

    private String userAgent;

    public Visit() {
    }

    public Visit(long time, IpPosition ipPosition,String userAgent) {
        LocalDateTime ldt =
                Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.time = ldt;
        this.ipPosition = ipPosition;
        this.userAgent = userAgent;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public long timeAsMillis() {
        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public IpPosition getIpPosition() {
        return ipPosition;
    }

    public void setIpPosition(IpPosition ipPosition) {
        this.ipPosition = ipPosition;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static Visit fromJSON(String json) throws IOException {
        return new ObjectMapper().readValue(json,Visit.class);
    }
}
