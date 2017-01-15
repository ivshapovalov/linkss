package ru.ivan.linkss.repository;


public class FullLink {

    private String key;
    private String shortLink;
    private String link;
    private String count;
    private String imageLink;
    private String userName;

    public FullLink(String key, String shortLink, String link, String count, String imageLink,
                    String userName) {
        this.key=key;
        this.shortLink = shortLink;
        this.link = link;
        this.count = count;
        this.imageLink = imageLink;
        this.userName = userName;
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

    public String getCount() {
        return count;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getUserName() {
        return userName;
    }
}
