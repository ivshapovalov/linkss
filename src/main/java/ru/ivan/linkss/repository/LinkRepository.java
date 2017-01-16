package ru.ivan.linkss.repository;

import java.util.List;

public interface LinkRepository {
    String createShortLink(String autorizedUser, String link);

    void createUser(String userName, String password);

    String getLink(String key);

    String getRandomShortLink();

    List<List<String>> getShortStat();

    List<List<String>> getShortStat(String autorizedUser);

    List<FullLink> getFullStat(String contextPath);

    List<FullLink> getFullStat(String userName, String contextPath);

    boolean checkUser(User user);

    void deleteUserLink(User user, String shortLink, String owner);

    List<User> getUsers();

    User getUser(User autorizedUser, String userName);

    void updateUser(User autorizedUser, User newUser, User oldUser);
}
