package ru.ivan.linkss.repository.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Visit {

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime time;

    private IpPosition ipPosition;

    public Visit() {
    }

    public Visit(LocalDateTime time, IpPosition ipPosition) {
        this.time = time;
        this.ipPosition = ipPosition;
    }
    public Visit(long time, IpPosition ipPosition) {
        LocalDateTime ldt =
                Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.time = ldt;
        this.ipPosition = ipPosition;
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
}
