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

    private IpLocation ipLocation;

    public Visit() {
    }

    public Visit(LocalDateTime time, IpLocation ipLocation) {
        this.time = time;
        this.ipLocation = ipLocation;
    }
    public Visit(long time, IpLocation ipLocation) {
        LocalDateTime ldt =
                Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.time = ldt;
        this.ipLocation = ipLocation;
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

    public IpLocation getIpLocation() {
        return ipLocation;
    }

    public void setIpLocation(IpLocation ipLocation) {
        this.ipLocation = ipLocation;
    }
}
