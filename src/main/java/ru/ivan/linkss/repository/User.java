package ru.ivan.linkss.repository;

/**
 * Created by Ivan on 14.01.2017.
 */
public class User {
    private String userName;
    private String password;
    private boolean isAdmin;
    private boolean isEmpty=true;
    private long linkNumber;

    public User() {
    }

    public User(String userName, String password, long linkNumber) {
        this.userName = userName;
        this.password = password;
        this.linkNumber = linkNumber;
    }

    public long getLinkNumber() {
        return linkNumber;
    }

    public void setLinkNumber(long linkNumber) {
        this.linkNumber = linkNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }
}
