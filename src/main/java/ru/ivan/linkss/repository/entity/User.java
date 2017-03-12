package ru.ivan.linkss.repository.entity;

public class User {
    private String userName;
    private String password = "";
    private String email = "";
    private boolean admin;
    private boolean empty = true;

    public User(UserBuilder builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.email = builder.email;
        this.admin = builder.admin;
        this.empty = builder.empty;
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

    public static class UserBuilder {
        private String userName;
        private String password = "";
        private String email = "";
        private boolean admin;
        private boolean empty = true;

        public UserBuilder() {
        }

        public UserBuilder addUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserBuilder addPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder addEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder addIsAdmin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public UserBuilder addisEmpty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
