package ru.ivan.linkss.repository.entity;


public class FullLink {

    private String key;
    private String shortLink;
    private String link;
    private String visits;
    private String imageLink;
    private String userName;
    private long days;

    public FullLink() {

    }

    public FullLink(String key, String shortLink, String link, String visits, String imageLink,
                    String userName, long days) {
        this.key=key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
        this.days = days;
    }

    public FullLink(String key, String shortLink, String link, String visits, String imageLink,
                    String userName) {
        this.key=key;
        this.shortLink = shortLink;
        this.link = link;
        this.visits = visits;
        this.imageLink = imageLink;
        this.userName = userName;
    }


    public long getDays() {
        return days;
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

    public void setDays(long days) {
        this.days = days;
    }

}