package ru.ivan.linkss.repository.entity;

public class User {
    private String userName;
    private String password = "";
    private String email = "";
    private boolean admin;
    private boolean empty = true;
    private IpLocation ipLocation;

    public User(Builder builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.email = builder.email;
        this.admin = builder.admin;
        this.empty = builder.empty;
        this.ipLocation = builder.ipLocation;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", admin=" + admin +
                '}';
    }

    public static class Builder {
        private String userName;
        private String password = "";
        private String email = "";
        private boolean admin;
        private boolean empty = true;
        private IpLocation ipLocation;

        public Builder() {
        }

        public Builder addUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder addPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder addEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder addIsAdmin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public Builder addIsEmpty(boolean empty) {
            this.empty = empty;
            return this;
        }
        public Builder addIPLocation(IpLocation location) {
            this.ipLocation = ipLocation;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
