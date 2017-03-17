package ru.ivan.linkss.repository.entity;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import ru.ivan.linkss.util.Util;

import java.time.LocalDateTime;

public class FullLink {

    private String key;
    private String shortLink;
    private String link;
    private String visits;
    private String imageLink;
    private String userName;
    private long seconds;
    private IpPosition ipPosition;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime deleted;

    public FullLink() {

    }

    public FullLink(Builder builder) {
        this.key = builder.key;
        this.shortLink = builder.shortLink;
        this.link = builder.link;
        this.visits = builder.visits;
        this.deleted = builder.deleted;
        this.imageLink = builder.imageLink;
        this.userName = builder.userName;
        this.seconds = builder.seconds;
        this.ipPosition = builder.ipPosition;
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

    public static class Builder {
        private String key;
        private String shortLink;
        private String link;
        private String visits;
        private String imageLink;
        private String userName;
        private long seconds;
        private IpPosition ipPosition;
        private LocalDateTime deleted;

        public Builder() {
        }

        public Builder addKey(String key) {
            this.key = key;
            return this;
        }

        public Builder addShortLink(String shortLink) {
            this.shortLink = shortLink;
            return this;
        }

        public Builder addLink(String link) {
            this.link = link;
            return this;
        }

        public Builder addVisits(String visits) {
            this.visits = visits;
            return this;
        }

        public Builder addImageLink(String imageLink) {
            this.imageLink = imageLink;
            return this;
        }

        public Builder addUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder addSeconds(long seconds) {
            this.seconds = seconds;
            return this;
        }

        public Builder addIPLocation(IpPosition ipPosition) {
            this.ipPosition = ipPosition;
            return this;
        }
        public Builder addDeleted(LocalDateTime deleted) {
            this.deleted = deleted;
            return this;
        }

        public FullLink build() {
            return new FullLink(this);
        }
    }
}
