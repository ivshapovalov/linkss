package ru.ivan.linkss.repository.entity;


import ru.ivan.linkss.util.Util;

public class FullLink {

    private String key;
    private String shortLink;
    private String link;
    private String visits;
    private String imageLink;
    private String userName;
    private long seconds;

    public FullLink() {

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
                    String userName) {
        this.key = key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
    }


    public long getSeconds() {
        return seconds;
    }

    public String getSecondsAsPeriod() {
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
