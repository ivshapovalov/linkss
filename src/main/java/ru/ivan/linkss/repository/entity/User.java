package ru.ivan.linkss.repository.entity;

public class User {
    private String userName;
    private String password = "";
    private String email = "";
    private boolean isAdmin;
    private boolean isEmpty = true;

    public User(UserBuilder builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.email = builder.email;
        this.isAdmin = builder.isAdmin;
        this.isEmpty = builder.isEmpty;
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
        isAdmin = admin;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

    public static class UserBuilder {
        private String userName;
        private String password = "";
        private String email = "";
        private boolean isAdmin;
        private boolean isEmpty = true;

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

        public UserBuilder addIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public UserBuilder addisEmpty(boolean isEmpty) {
            this.isEmpty = isEmpty;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
