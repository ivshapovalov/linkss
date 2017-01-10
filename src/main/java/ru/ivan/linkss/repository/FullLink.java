package ru.ivan.linkss.repository;


public class FullLink {

    private String shortLink;
    private String link;
    private String count;
    private String imageLink;

    public FullLink(String shortLink, String link, String count, String imageLink) {
        this.shortLink = shortLink;
        this.link = link;
        this.count = count;
        this.imageLink = imageLink;
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
}
