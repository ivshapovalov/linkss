package ru.ivan.linkss.repository;

import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.repository.entity.UserDTO;

import java.math.BigInteger;
import java.util.List;

public interface LinkRepository {

    void init();

    long getDBLinksSize();

    long getDBFreeLinksSize();

    long getDomainsSize(User autorizedUser);

    long getUsersSize(User autorizedUser);

    String createShortLink(User autorizedUser, String link);

    void createUser(User user);

    String getLink(String key);

    String visitLink(String key);

    String getRandomShortLink();

    List<Domain> getShortStat(int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String contextPath, int offset, int recordsOnPage);

    List<FullLink> getUserLinks(String userName, String contextPath, int offset, int
            recordsOnPage);

    List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage);

    User checkUser(User user);

    void deleteLink(User user, String shortLink, String owner);

    void deleteArchiveLink(User user, String shortLink, String owner);

    void restoreArchiveLink(User user, String shortLink, String owner);

    void deleteFreeLink(String shortLink);

    List<UserDTO> getUsersDTO(int offset, int recordsOnPage);

    List<String> getFreeLinks(int offset, int recordsOnPage);

    User getUser(User autorizedUser, String userName);

    void updateUser(User autorizedUser, User newUser, User oldUser);

    void deleteUser(User autorizedUser, String userName);

    void clearUser(User autorizedUser, String userName);

    BigInteger checkFreeLinksDB() throws Exception;

    BigInteger deleteExpiredUserLinks() throws Exception;

    BigInteger createKeys(final int length);

    long getUserLinksSize(User autorizedUser, String owner);

    long getUserArchiveSize(User autorizedUser, String owner);

    long getLinkExpirePeriod(String shortLink);

    FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath);

    void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink);

    void clear();
}
