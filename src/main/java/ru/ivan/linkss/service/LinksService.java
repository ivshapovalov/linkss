package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


public interface LinksService {

    String getRandomShortLink();

    String createShortLink(User autorizedUser, String link, String path, String context);

    void createUser(String userName, String password);

    String visitLink(String shortLink);

    String getLink(String shortLink);

    long getLinkExpirePeriod(String shortLink);

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getFullStat(String userName,String contextPath, int offset, int recordsOnPage);

    void createQRImage(String path,String shortLink, String fullShortLink) throws
            WriterException,
            IOException;
     void sendFileToS3(String imagePath,String key) ;

        boolean checkUser(User user);

    void deleteUserLink(User user, String shortLink, String owner);

    List<User> getUsers(int offset, int recordsOnPage);

    List<String> getFreeLinks(int offset, int recordsOnPage);

    User getUser(User autorizedUser, String userName);

    void deleteUser(User autorizedUser, String userName);

    void clearUser(User autorizedUser, String userName);

    void updateUser(User autorizedUser, User newUser, User oldUser);

    BigInteger updateFreeLinks() throws Exception;

    BigInteger deleteExpiredUserLinks() throws Exception;

    long getDBLinksSize();

    long getDBFreeLinksSize();

    long getDomainsSize(User autorizedUser);

    long getUsersSize(User autorizedUser);

    long getUserLinksSize(User autorizedUser, String owner);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner,String contextPath);

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink);

    void downloadImageFromS3(String filePath, String key);

    void deleteFreeLink(String shortLink);
}
