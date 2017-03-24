package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import ru.ivan.linkss.repository.RepositoryException;
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
                           Map<String,String> params) throws RepositoryException;

    void createUser(User user,Map<String,String> params) throws RepositoryException;

    void sendVerifyMail(User user, String path);

    List<User> sendRemindMail(User user, String verifyPath) throws RepositoryException;

    String visitLink(String shortLink,Map<String,String> params);

    String visitLinkwithIpChecking(String shortLink,Map<String,String> params);

    Link getLink(String shortLink);

    long getLinkExpirePeriod(String shortLink);

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String userName, String contextPath, int offset, int recordsOnPage);

    List<Visit> getLinkVisits(User autorizedUser,String owner, String key,int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getDomainActualVisits(User autorizedUser,String key,int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getDomainHistoryVisits(User autorizedUser,String key,int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getUserVisits(User autorizedUser,String owner);

    List<Visit> getAllVisits();

    List<Link> getAllLinks();

    List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage);

    void createQRImage(String path, String shortLink, String fullShortLink) throws
            WriterException,
            IOException;

    User checkUser(User user) throws RepositoryException;

    boolean checkLinkOwner(String key,String owner);

    void deleteUserLink(User user, String shortLink, String owner) throws RepositoryException;

    void deleteLinkVisit(User user, String owner, String key, String time ) throws RepositoryException;

    void deleteArchiveLink(User user, String shortLink, String owner,String path) throws RepositoryException;

    void restoreArchiveLink(User user, String shortLink, String owner) throws RepositoryException;

    List<UserDTO> getUsersDTO(int offset, int recordsOnPage);

    List<String> getFreeLinks(int offset, int recordsOnPage);

    User getUser(User autorizedUser, String userName) throws RepositoryException;

    void deleteUser(User autorizedUser, String userName) throws RepositoryException;

    void clearUser(User autorizedUser, String userName) throws RepositoryException;

    void updateUser(User autorizedUser, User newUser, User oldUser) throws RepositoryException;

    BigInteger updateFreeLinks() throws Exception;

    BigInteger deleteExpiredUserLinks() throws Exception;

    long getDBLinksSize();

    long getDBFreeLinksSize();

    long getDomainsActualSize(User autorizedUser) throws RepositoryException;

    long getDomainsHistorySize(User autorizedUser) throws RepositoryException;

    long getUsersSize(User autorizedUser) throws RepositoryException;

    long getVisitsActualSize(User autorizedUser) throws RepositoryException;

    long getVisitsHistorySize(User autorizedUser) throws RepositoryException;

    long getUserLinksSize(User autorizedUser, String owner) throws RepositoryException;

    long getUserArchiveSize(User autorizedUser, String owner) throws RepositoryException;

    long getLinkVisitsSize(User autorizedUser, String owner, String key) throws RepositoryException;

    long getDomainActualVisitsSize(User autorizedUser, String key) throws
            RepositoryException;

    long getDomainHistoryVisitsSize(User autorizedUser, String key) throws
            RepositoryException;

    long getUserVisitsSize(User autorizedUser, String owner);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) throws RepositoryException;

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) throws RepositoryException;

    void deleteFreeLink(String shortLink);

    String verifyUser(String uuid);
}
