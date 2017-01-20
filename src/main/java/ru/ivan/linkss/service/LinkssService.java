package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import ru.ivan.linkss.repository.FullLink;
import ru.ivan.linkss.repository.User;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public interface LinkssService {

    String getRandomShortLink();

    String createShortLink(String user, String link);

    void createUser(String userName, String password);

    String getLink(String shortLink);

    List<List<String>> getShortStat();

    List<FullLink> getFullStat(String contextPath);

    List<FullLink> getFullStat(String userName,String contextPath, int offset, int recordsOnPage);

    void createQRImage(String path,String shortLink, String fullShortLink) throws
            WriterException,
            IOException;

    boolean checkUser(User user);

    void deleteUserLink(User user, String shortLink, String owner);

    List<User> getUsers();

    User getUser(User autorizedUser, String userName);

    void deleteUser(User autorizedUser, String userNme);

    void updateUser(User autorizedUser, User newUser, User oldUser);

    BigInteger updateFreeLinks();

    long getDBLinksSize();

    long getDBFreeLinksSize();

    void updateUserLinkDays(User autorizedUser, String shortLink, String owner, long days);

    long getUserLinksSize(User autorizedUser, String owner);
}
