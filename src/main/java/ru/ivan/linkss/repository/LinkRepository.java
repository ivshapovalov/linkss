package ru.ivan.linkss.repository;

import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;

import java.math.BigInteger;
import java.util.List;

public interface LinkRepository {

    void init();

    long getDBLinksSize();

    long getDBFreeLinksSize();

    long getDomainsSize(User autorizedUser);

    long getUsersSize(User autorizedUser);

    String createShortLink(User autorizedUser, String link);

    void createUser(String userName, String password);

    String getLink(String key);

    String visitLink(String key);

    String getRandomShortLink();

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getFullStat(String contextPath, int offset, int recordsOnPage);

    List<FullLink> getFullStat(String userName, String contextPath, int offset, int
            recordsOnPage);

    boolean checkUser(User user);

    void deleteUserLink(User user, String shortLink, String owner);

    List<User> getUsers(int offset, int recordsOnPage);

    User getUser(User autorizedUser, String userName);

    void updateUser(User autorizedUser, User newUser, User oldUser);

    void deleteUser(User autorizedUser, String userName);

    BigInteger checkFreeLinksDB();

    BigInteger deleteExpiredUserLinks();

    BigInteger createKeys(final int length);

    long getUserLinksSize(User autorizedUser, String owner);

    long getLinkDays(String shortLink);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath);

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink);

}
