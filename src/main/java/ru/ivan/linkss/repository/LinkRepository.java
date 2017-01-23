package ru.ivan.linkss.repository;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public interface LinkRepository {

    void init();

    long getDBLinksSize();

    long getDBFreeLinksSize();

    String createShortLink(String autorizedUser, String link);

    void createUser(String userName, String password);

    String getLink(String key);

    String getRandomShortLink();

    List<List<String>> getShortStat();

    List<FullLink> getFullStat(String contextPath);

    default List<FullLink> getFullStat(String userName, String contextPath, int offset, int
            recordsOnPage){
        return Collections.emptyList();
    };

    boolean checkUser(User user);

    void deleteUserLink(User user, String shortLink, String owner);

    List<User> getUsers();

    User getUser(User autorizedUser, String userName);

    void updateUser(User autorizedUser, User newUser, User oldUser);

    void deleteUser(User autorizedUser, String userName);

    BigInteger checkFreeLinksDB();

    BigInteger createKeys(final int length);

    default void updateUserLinkDays(User autorizedUser, String shortLink, String owner, long
            days) {

    };

    default long getUserLinksSize(User autorizedUser, String owner) {return 0;};

    default long getLinkDays(String shortLink) {
       return  0;
    }
}
