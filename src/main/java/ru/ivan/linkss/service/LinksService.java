package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import ru.ivan.linkss.repository.entity.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface LinksService {

    String getRandomShortLink();

    void clear(String path);

    void deleteAllImages(String path);

    BigInteger deleteLocalImages(String path);

    String createShortLink(User autorizedUser, String link, String path, String context,
                           Map<String,String> params);

    void createUser(User user);

    String visitLink(String shortLink,Map<String,String> params);

    String visitLinkwithIpChecking(String shortLink,Map<String,String> params);

    Link getLink(String shortLink);

    long getLinkExpirePeriod(String shortLink);

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String userName, String contextPath, int offset, int recordsOnPage);

    List<Visit> getLinkVisits(User autorizedUser,String owner, String key,int offset, long
            recordsOnPage);

    List<Visit> getUserVisits(User autorizedUser,String owner);

    List<Visit> getAllVisits();

    List<Link> getAllLinks();

    List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage);

    void createQRImage(String path, String shortLink, String fullShortLink) throws
            WriterException,
            IOException;

    User checkUser(User user);

    boolean checkLinkOwner(String key,String owner);

    void deleteUserLink(User user, String shortLink, String owner);

    void deleteLinkVisit(User user, String owner, String key, String time );

    void deleteArchiveLink(User user, String shortLink, String owner,String path);

    void restoreArchiveLink(User user, String shortLink, String owner);

    List<UserDTO> getUsersDTO(int offset, int recordsOnPage);

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

    long getVisitsActualSize(User autorizedUser);

    long getVisitsHistorySize(User autorizedUser);

    long getUserLinksSize(User autorizedUser, String owner);

    long getUserArchiveSize(User autorizedUser, String owner);

    long getLinkVisitsSize(User autorizedUser, String owner, String key);

    long getUserVisitsSize(User autorizedUser, String owner);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath);

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink);

    void deleteFreeLink(String shortLink);
}
