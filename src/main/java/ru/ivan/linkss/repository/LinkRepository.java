package ru.ivan.linkss.repository;

import ru.ivan.linkss.repository.entity.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface LinkRepository {

    void init();

    long getDBLinksSize();

    long getDBFreeLinksSize();

    long getDomainsActualSize(User autorizedUser) throws RepositoryException;
    long getDomainsHistorySize(User autorizedUser) throws RepositoryException;

    long getUsersSize(User autorizedUser) throws RepositoryException;

    long getVisitsByDomainActualSize(User autorizedUser) throws RepositoryException;

    long getVisitsByDomainHistorySize(User autorizedUser) throws RepositoryException;

    String createShortLink(User autorizedUser, String link, IpPosition ipPosition) throws RepositoryException;

    void createUser(User user) throws RepositoryException;

    Link getLink(String key);

    String visitLink(String key, IpPosition ipPosition,Map<String,String> params);

    String getRandomShortLink();

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String contextPath, int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String userName, String contextPath, int offset, int
            recordsOnPage);
    List<Visit> getLinkVisits(User autorizedUser,String owner,String key, int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getDomainActualVisits(User autorizedUser,String key, int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getDomainHistoryVisits(User autorizedUser,String key, int offset, long
            recordsOnPage) throws RepositoryException;

    List<Visit> getUserVisits(User autorizedUser,String owner);

    List<Visit> getAllVisits();

    List<Link> getAllLinks();

    List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage);

    User checkUser(User user) throws RepositoryException;

    boolean checkLinkOwner(String key,String owner);

    void deleteLink(User user, String shortLink, String owner) throws RepositoryException;

    void deleteVisit(User user, String owner, String key, String time) throws RepositoryException;

    void deleteArchiveLink(User user, String shortLink, String owner) throws RepositoryException;

    void restoreArchiveLink(User user, String shortLink, String owner) throws RepositoryException;

    void deleteFreeLink(String shortLink);

    List<UserDTO> getUsersDTO(int offset, int recordsOnPage);

    List<String> getFreeLinks(int offset, int recordsOnPage);

    User getUser(User autorizedUser, String userName) throws RepositoryException;

    List<User> getUsers(String email) throws RepositoryException;

    void updateUser(User autorizedUser, User newUser, User oldUser) throws RepositoryException;

    void deleteUser(User autorizedUser, String userName) throws RepositoryException;

    void clearUser(User autorizedUser, String userName) throws RepositoryException;

    BigInteger checkFreeLinksDB() throws Exception;

    BigInteger deleteExpiredUserLinks() throws Exception;

    BigInteger createKeys(final int length);

    long getUserLinksSize(User autorizedUser, String owner) throws RepositoryException;

    long getUserArchiveSize(User autorizedUser, String owner) throws RepositoryException;

    long getLinkVisitsSize(User autorizedUser, String owner
                           ,String key) throws RepositoryException;

    long getDomainActualVisitsSize(User autorizedUser, String key) throws RepositoryException;

    long getDomainHistoryVisitsSize(User autorizedUser, String key) throws RepositoryException;

    long getUserVisitsSize(User autorizedUser, String ow
                           );

    long getLinkExpirePeriod(String shortLink);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) throws RepositoryException;

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) throws RepositoryException;

    void clear();

    String generateNewUUID(User user);

    String verifyUser(String uuid);

    String getUserUUID(User user);
}
