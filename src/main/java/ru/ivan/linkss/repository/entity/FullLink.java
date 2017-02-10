package ru.ivan.linkss.repository.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.format.annotation.DateTimeFormat;
import ru.ivan.linkss.util.Util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class FullLink {

    private String key;
    private String shortLink;
    private String link;
    private String visits;
    private String imageLink;
    private String userName;
    private long seconds;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime deleted;

    public FullLink() {

    }
    public FullLink(String key, String link) {
        this.key = key;
        this.link = link;
    }

    public FullLink(LocalDateTime deleted) {
        this.deleted = deleted;
    }

    public FullLink(String key, String shortLink, String link, String visits, LocalDateTime deleted, String imageLink, String userName, long seconds) {
        this.key = key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.deleted = deleted;
        this.imageLink = imageLink;
        this.userName = userName;
        this.seconds = seconds;
    }

    public FullLink(String key, String link, String userName) {
        this.key = key;
        this.link = link;
        this.userName = userName;
    }

    public FullLink(String key, String shortLink, String link, String visits, String imageLink,
                    String userName, long seconds) {
        this.key = key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
        this.seconds = seconds;
    }
    public FullLink(String key, String shortLink, String link, String visits, String imageLink,
                    String userName, LocalDateTime deleted) {
        this.key = key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
        this.deleted = deleted;
    }

    public FullLink(String key, String shortLink, String link, String visits, String imageLink,
                    String userName) {
        this.key = key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
    }

    public FullLink(String key, String link, String userName, String visits, LocalDateTime
            deleted) {
        this.key = key;
        this.link = link;
        this.visits = visits;
        this.deleted = deleted;
        this.userName = userName;
    }

    public LocalDateTime getDeleted() {
        return deleted;
    }

    public void setDeleted(LocalDateTime deleted) {
        this.deleted = deleted;
    }

    public long getSeconds() {
        return seconds;
    }

    public String convertSecondsToPeriod() {
        if (seconds == 0) {
            return "";
        } else {
            return Util.convertSecondsToPeriod(seconds);
        }
    }

    public String getKey() {
        return key;
    }

    public String getShortLink() {
        return shortLink;
    }

    public String getLink() {
        return link;
    }

    public String getVisits() {
        return visits;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getUserName() {
        return userName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setShortLink(String shortLink) {
        this.shortLink = shortLink;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return "FullLink{" +
                "key='" + key + '\'' +
                ", shortLink='" + shortLink + '\'' +
                ", link='" + link + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
