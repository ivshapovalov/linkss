package ru.ivan.linkss.repository.entity;

public class User {
    private String userName;
    private String password="";
    private String email="";
    private boolean isAdmin;
    private boolean isEmpty=true;
    private long linkNumber;

    public User() {

    }

    public User(String userName) {
        this.userName = userName;
    }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public User(String userName, String password, boolean isAdmin, boolean isEmpty) {
        this.userName = userName;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isEmpty = isEmpty;
    }

    public User(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
    }


    public User(String userName, String password, long linkNumber) {
        this.userName = userName;
        this.password = password;
        this.linkNumber = linkNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
