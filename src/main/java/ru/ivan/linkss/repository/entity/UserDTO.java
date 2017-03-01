package ru.ivan.linkss.repository.entity;

public class UserDTO {
    private User user;
    private long linkCount;
    private long archiveCount;

    public UserDTO() {

    }

    public UserDTO(User user, long linkCount, long archiveCount) {
        this.user = user;
        this.linkCount = linkCount;
        this.archiveCount = archiveCount;
    }

    public User getUser() {
        return user;
    }

    public String getUserName() {
        return user.getUserName();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(long linkCount) {
        this.linkCount = linkCount;
    }

    public long getArchiveCount() {
        return archiveCount;
    }

    public void setArchiveCount(long archiveCount) {
        this.archiveCount = archiveCount;
    }
}
