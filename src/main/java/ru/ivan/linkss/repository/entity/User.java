package ru.ivan.linkss.repository.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class User {
    private String userName;
    private String password = "";
    private String email = "";
    private boolean admin;
    private boolean empty = true;
    private IpPosition ipPosition;

    public User(Builder builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.email = builder.email;
        this.admin = builder.admin;
        this.empty = builder.empty;
        this.ipPosition = builder.ipPosition;
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

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public static User fromJSON(String json) throws IOException {
        return   new ObjectMapper().readValue(json,User.class);
    }

    public static class Builder {
        private String userName;
        private String password = "";
        private String email = "";
        private boolean admin;
        private boolean empty = true;
        private IpPosition ipPosition;

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
        public Builder addIPLocation(IpPosition location) {
            this.ipPosition = ipPosition;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
