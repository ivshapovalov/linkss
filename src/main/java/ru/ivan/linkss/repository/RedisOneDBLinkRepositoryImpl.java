package ru.ivan.linkss.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.repository.entity.UserDTO;
import ru.ivan.linkss.util.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Repository
@Qualifier(value = "repositoryOne")
public class RedisOneDBLinkRepositoryImpl implements LinkRepository {

    //REDIS 1
    private static final int DB_WORK_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;
    private static final int DB_LINK_NUMBER = 2;
    private static final int DB_ARCHIVE_NUMBER = 3;

    private static final long MIN_FREE_LINK_SIZE = 10;
    private static final long SECONDS_IN_DAY = 3600 * 24;

    private static final String KEY_USERS = "_users";
    private static final String KEY_VISITS = "_visits";
    private static final String KEY_VISITS_BY_DOMAIN_ACTUAL = "_visits_by_domain_actual";
    private static final String KEY_VISITS_BY_DOMAIN_HISTORY = "_visits_by_domain_history";
    private static final String KEY_PREFERENCES = "_preferences";
    private static final String KEY_LENGTH = "key.length";
    private static final String KEY_EXPIRATION_PERIOD = "expiration.period";

    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "user";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admin";


    //private RedisClient redisClient = RedisClient.createShortLink("redis://localhost:6379/0");
//    private RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
////    private RedisClient redisClientStat = RedisClient.createShortLink
//            ("redis://h:p3c1e48009e2ca7405945e112b198385d800c695c79095312007c06ab48285e70@ec2-54-163-250-167.compute-1.amazonaws.com:18529");
    //   private RedisClient redisClientByUsers = RedisClient.createShortLink
//            ("redis://h:p3291e9f52c34dafdb323e02b34803df7a3b56ac1f3c993dfe3215096fb76b154@ec2-184-72-246-90.compute-1.amazonaws.com:21279");
    private final RedisClient redisClient;

    public RedisOneDBLinkRepositoryImpl() {
        //TODO убрать
        this.redisClient = RedisClient.create(System.getenv("REDIS_ONE_URL"));
        //this.redisClient = RedisClient.create("redis://localhost:6379/0");
    }

    public RedisOneDBLinkRepositoryImpl(RedisClient redisClient) {
        this.redisClient = redisClient;

    }

    @Override
    public void init() {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.flushall();
            syncCommands.select(DB_WORK_NUMBER);
            syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, String.valueOf("1"));
            createKeys(1);
            syncCommands.hset(KEY_PREFERENCES, KEY_EXPIRATION_PERIOD, String.valueOf("30"));
            String jAdmin = "";
            String jUser = "";
            try {
                jAdmin = new ObjectMapper().writeValueAsString(
                        new User.UserBuilder()
                                .addUserName(ADMIN_USER)
                                .addPassword(ADMIN_PASSWORD)
                                .addIsAdmin(true)
                                .addisEmpty(false).build());
                jUser = new ObjectMapper().writeValueAsString(
                        new User.UserBuilder()
                                .addUserName(DEFAULT_USER)
                                .addPassword(DEFAULT_PASSWORD)
                                .addIsAdmin(false)
                                .addisEmpty(false).build());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            syncCommands.hset(KEY_USERS, ADMIN_USER, jAdmin);
            syncCommands.hset(KEY_USERS, DEFAULT_USER, jUser);
        }
    }

    private StatefulRedisConnection<String, String> connect() {
        boolean connectionFailed = true;
        StatefulRedisConnection<String, String> connection = null;
        do {
            try {
                connection = redisClient.connect();
                connectionFailed = false;
            } catch (Exception e) {
                connectionFailed = true;
            }
        } while (connectionFailed);
        return connection;
    }

    @Override
    public long getDBLinksSize() {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            long size = syncCommands.dbsize();
            return size;
        }
    }

    @Override
    public long getDBFreeLinksSize() {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_FREELINK_NUMBER);
            long size = syncCommands.dbsize();
            return size;
        }
    }

    @Override
    public long getDomainsSize(User autorizedUser) {
        if (autorizedUser == null) {
            throw new RuntimeException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (autorizedUser == null) {
            throw new RuntimeException(String.format("User '%s' does not have permissions to see domains stat", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            long size = syncCommands.hlen(KEY_VISITS_BY_DOMAIN_ACTUAL);
            return size;
        }
    }

    @Override
    public long getUsersSize(User autorizedUser) {
        if (autorizedUser == null) {
            throw new RuntimeException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (autorizedUser == null) {
            throw new RuntimeException(String.format("User '%s' does not have permissions to see domains stat", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            long size = syncCommands.hlen(KEY_USERS);
            return size;
        }
    }

    @Override
    public List<Domain> getShortStat(int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            List<Domain> shortStat = syncCommands.hkeys(KEY_VISITS_BY_DOMAIN_HISTORY)
                    .stream()
                    .map(domain -> new Domain(domain, syncCommands.hget(KEY_VISITS_BY_DOMAIN_ACTUAL,
                            domain), syncCommands.hget(KEY_VISITS_BY_DOMAIN_HISTORY, domain)))
                    .sorted((d1, d2) -> Integer.parseInt(d2.getVisitsActual())
                            - Integer.parseInt(d1.getVisitsActual()))
                    .skip(offset)
                    .limit(recordsOnPage)
                    .collect(Collectors.toList());
            return shortStat;
        }
    }

    @Override
    public List<FullLink> getUserLinks(String contextPath, int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            List<String> links = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> links.add(key));
                cursor = syncCommands.scan(cursor);
            }
            syncCommands.select(DB_WORK_NUMBER);
            List<FullLink> fullStat = links
                    .stream()
                    .sorted()
                    .filter(user -> !user.startsWith("_"))
                    .map(user -> {
                        syncCommands.select(DB_WORK_NUMBER);
                        return syncCommands.hkeys(user)
                                .stream()
                                .map(shortLink -> {
                                    syncCommands.select(DB_LINK_NUMBER);
                                    String link = syncCommands.get(shortLink);
                                    String shortLinkWithContext = contextPath + shortLink;
                                    String visits = syncCommands.hget(KEY_VISITS, shortLink);
                                    return new FullLink(shortLink, shortLinkWithContext,
                                            link, visits,
                                            shortLinkWithContext + ".png", user);
                                }).collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .filter(fullLink -> fullLink.getLink() != null)
                    .sorted((fullLink1, fullLink2) ->
                    {
                        int result = fullLink1.getUserName().compareTo(fullLink2.getUserName());
                        if (result != 0) {
                            return result;
                        }
                        result = fullLink1.getKey().compareTo(fullLink2.getKey());
                        return result;
                    })
                    .skip(offset)
                    .limit(recordsOnPage)
                    .collect(Collectors.toList());
            return fullStat;
        }
    }

    @Override
    public List<FullLink> getUserLinks(String userName, String contextPath, int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            List<FullLink> fullStat = syncCommands.hkeys(userName)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(shortLink -> {
                        syncCommands.select(DB_LINK_NUMBER);
                        String link = syncCommands.get(shortLink);
                        String shortLinkWithContext = contextPath + shortLink;
                        syncCommands.select(DB_WORK_NUMBER);
                        String visits = syncCommands.hget(KEY_VISITS, shortLink);
                        syncCommands.select(DB_LINK_NUMBER);
                        long ttl = syncCommands.ttl(shortLink);
                        if (ttl < 0) {
                            return new FullLink(shortLink, shortLinkWithContext,
                                    link, visits,
                                    shortLinkWithContext + ".png", userName);
                        } else {
                            return new FullLink(shortLink, shortLinkWithContext,
                                    link, visits,
                                    shortLinkWithContext + ".png", userName, ttl);
                        }
                    })
                    .filter(fullLink -> fullLink.getLink() != null)
                    .collect(Collectors.toList());
            return fullStat;
        }
    }

    @Override
    public List<FullLink> getUserArchive(String userName, String contextPath, int offset, int
            recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_ARCHIVE_NUMBER);
            List<FullLink> archive = syncCommands.hkeys(userName)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(shortLink -> {
                        FullLink fullLink = null;
                        try {
                            fullLink = new ObjectMapper().readValue(syncCommands.hget(userName, shortLink)
                                    , FullLink
                                            .class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String shortLinkWithContext = contextPath + shortLink;
                        fullLink.setShortLink(shortLinkWithContext);
                        fullLink.setImageLink(shortLinkWithContext + ".png");
                        return fullLink;

                    })
                    .filter(fullLink -> fullLink.getLink() != null)
                    .collect(Collectors.toList());
            return archive;
        }
    }


    @Override
    public String getRandomShortLink() {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            String shortLink = syncCommands.randomkey();
            return shortLink;
        }
    }

    @Override
    public String createShortLink(User autorizedUser, String link) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            String period = syncCommands.hget(KEY_PREFERENCES, KEY_EXPIRATION_PERIOD);
            if (period == null) period = "0";
            syncCommands.select(DB_FREELINK_NUMBER);
            String shortLink = null;

            synchronized (redisClient) {
                boolean failed = false;
                do {
                    shortLink = syncCommands.randomkey();
                    failed = shortLink == null;
                    syncCommands.del(shortLink);
                } while (failed);
            }

            syncCommands.select(DB_LINK_NUMBER);
            syncCommands.set(shortLink, link);
            int days = 0;
            if (!"".equals(period)) {
                syncCommands.expire(shortLink, Integer.parseInt(period) * SECONDS_IN_DAY);
            }
            syncCommands.select(DB_WORK_NUMBER);

            if (autorizedUser != null && !"".equals(autorizedUser.getUserName())) {
                try {
                    syncCommands.hset(autorizedUser.getUserName(), shortLink, link);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Cannot add shortlink '%s' to users " +
                            "'%s' links", shortLink, autorizedUser.getUserName()));
                }
            } else {
                syncCommands.hset(DEFAULT_USER, shortLink, link);

            }
            syncCommands.hset(KEY_VISITS, shortLink, "0");
            String domainName = "";

            domainName = Util.getDomainName(link);
            synchronized (redisClient) {
                if (!domainName.equals("")
                        && (syncCommands.hget(KEY_VISITS_BY_DOMAIN_HISTORY, domainName) == null
                )) {
                    syncCommands.hset(KEY_VISITS_BY_DOMAIN_HISTORY, domainName, "0");
                    syncCommands.hset(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName, "0");
                }
            }
            return shortLink;
        }
    }

    @Override
    public void clear() {
        init();
    }

    @Override
    public void createUser(User user) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (user.getUserName().contains("_")) {
                throw new RuntimeException(String.format("User name '%s' contains symbol '_'. Try " +
                                "another name",
                        user.getUserName()));
            }

            synchronized (redisClient) {
                if (syncCommands.hexists(KEY_USERS, user.getUserName())) {
                    throw new RuntimeException(String.format("User with name '%s' already exists. Try " +
                                    "another name",
                            user.getUserName()));
                }
                String json = "";
                try {
                    user.setEmpty(false);
                    json = new ObjectMapper().writeValueAsString(user);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                syncCommands.hset(KEY_USERS, user.getUserName(), json);
            }
        }
    }

    @Override
    public List<UserDTO> getUsersDTO(int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            List<UserDTO> usersDTO = syncCommands.hkeys(KEY_USERS)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(userName -> {
                        User user = null;
                        try {
                            user = new ObjectMapper().readValue(syncCommands.hget(KEY_USERS, userName), User.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        long linkCount = syncCommands.hlen(userName);
                        syncCommands.select(DB_ARCHIVE_NUMBER);
                        long archiveCount = syncCommands.hlen(userName);
                        UserDTO userDTO = new UserDTO(user, linkCount, archiveCount);
                        syncCommands.select(DB_WORK_NUMBER);
                        return userDTO;
                    })
                    .collect(Collectors.toList());
            return usersDTO;
        }
    }

    @Override
    public List<String> getFreeLinks(int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_FREELINK_NUMBER);
            List<String> freeLinks = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> freeLinks.add(key));
                cursor = syncCommands.scan(cursor);
            }

            return freeLinks.stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage).collect(Collectors.toList());
        }
    }

    @Override
    public User checkUser(User user) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, user.getUserName())) {
                throw new RuntimeException(String.format("User with name '%s' is not exists",
                        user.getUserName()));
            }
            User dbUser = null;
            try {
                dbUser = new ObjectMapper().readValue(syncCommands.hget(KEY_USERS, user
                        .getUserName()), User
                        .class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!user.getPassword().equals(dbUser.getPassword())) {
                throw new RuntimeException(String.format("Password for user '%s' is " +
                                "wrong",
                        user.getUserName()));
            }
            return dbUser;
        }
    }

    @Override
    public void deleteLink(User autorizedUser, String shortLink, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, autorizedUser.getUserName())) {
                throw new RuntimeException(String.format("User '%s' is not exists",
                        autorizedUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RuntimeException(String.format("User '%s' does not have link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            syncCommands.select(DB_LINK_NUMBER);
            String link = syncCommands.get(shortLink);
            if (link != null) {
                decreaseVisits(shortLink, owner, link, syncCommands);
                syncCommands.select(DB_LINK_NUMBER);
                syncCommands.del(shortLink);
            }
        }
    }

    @Override
    public void deleteArchiveLink(User autorizedUser, String shortLink, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);
            if (syncCommands.exists(owner) != 1) {
                throw new RuntimeException(String.format("Archive of user '%s' is not exists",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RuntimeException(String.format("User '%s' does not have archive " +
                                    "link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            syncCommands.hdel(owner, shortLink);
        }
    }

    @Override
    public void restoreArchiveLink(User autorizedUser, String shortLink, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);
            if (syncCommands.exists(owner) != 1) {
                throw new RuntimeException(String.format("Archive of user '%s' is not exists",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RuntimeException(String.format("User '%s' does not have archive " +
                                    "link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            FullLink fullLink = null;
            try {
                fullLink = new ObjectMapper().readValue(syncCommands.hget(owner, shortLink),
                        FullLink
                                .class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            synchronized (redisClient) {
                syncCommands.select(DB_ARCHIVE_NUMBER);
                String user = syncCommands.hget(KEY_USERS, fullLink.getUserName());
                if (user == null) {
                    syncCommands.select(DB_WORK_NUMBER);
                    if (syncCommands.hget(KEY_USERS, fullLink.getUserName()) == null) {
                        String json = "";
                        try {
                            json = new ObjectMapper().writeValueAsString(new User.UserBuilder()
                                    .addUserName(fullLink.getUserName())
                                    .addIsAdmin(false)
                                    .addisEmpty(false).build());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        syncCommands.hset(KEY_USERS, fullLink.getUserName(), json);
                    }
                } else {
                    if (syncCommands.hget(KEY_USERS, fullLink.getUserName()) == null) {
                        syncCommands.hset(KEY_USERS, fullLink.getUserName(), user);
                    }
                }

                syncCommands.hset(fullLink.getUserName(), fullLink.getKey(), fullLink
                        .getLink());
                if (!syncCommands.hexists(KEY_VISITS, shortLink)) {
                    syncCommands.hset(KEY_VISITS, shortLink, "0");
                }
                long visits = Long.parseLong(fullLink.getVisits());
                syncCommands.hincrby(KEY_VISITS, shortLink, visits);

                String domainName = Util.getDomainName(fullLink.getLink());
                if (!syncCommands.hexists(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName)) {
                    syncCommands.hset(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName, "0");
                }
                if (!"".equals(fullLink.getLink())) {
                    syncCommands.hincrby(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName, visits);
                }

                syncCommands.select(DB_ARCHIVE_NUMBER);
                syncCommands.select(DB_LINK_NUMBER);
                syncCommands.set(fullLink.getKey(), fullLink.getLink());

                syncCommands.select(DB_ARCHIVE_NUMBER);
                syncCommands.hdel(owner, shortLink);
            }

        }
    }

    @Override
    public void deleteFreeLink(String shortLink) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_FREELINK_NUMBER);
            synchronized (redisClient) {
                if (syncCommands.exists(new String[]{shortLink}) == 1) {
                    syncCommands.del(shortLink);
                }
            }
        }
    }

    private void decreaseVisits(String shortLink, String owner, String link, RedisCommands<String,
            String> syncCommands) {

        synchronized (redisClient) {
            syncCommands.select(DB_WORK_NUMBER);
            String visits = syncCommands.hget(KEY_VISITS, shortLink);
            syncCommands.hdel(KEY_VISITS, shortLink);
            if (visits == null) visits = "0";
            String domainName = Util.getDomainName(link);
            String visitsByDomainActual = syncCommands.hget
                    (KEY_VISITS_BY_DOMAIN_ACTUAL, domainName);
            if (visitsByDomainActual != null && !"".equals(visitsByDomainActual)
                    && Integer.parseInt(visitsByDomainActual) != 0) {
                int visitsCount = Integer.parseInt(visits);
                int visitsCountByDomainActual = Integer.parseInt(visitsByDomainActual);
                int decrement = visitsCount > visitsCountByDomainActual
                        ? visitsCountByDomainActual : visitsCount;
                syncCommands.hincrby(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName,
                        -1 * decrement);
            }
            syncCommands.hdel(owner, shortLink);
            syncCommands.select(DB_ARCHIVE_NUMBER);
            FullLink fullLink = new FullLink(shortLink, link, owner, visits, LocalDateTime.now());
            String json = "";
            try {
                json = new ObjectMapper().writeValueAsString(fullLink);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            syncCommands.hset(owner, shortLink, json);

        }
    }

    @Override
    public String getLink(String shortLink) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            String link = syncCommands.get(shortLink);
            return link;
        }
    }

    @Override
    public String visitLink(String shortLink) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_LINK_NUMBER);
            String link = syncCommands.get(shortLink);
            if (link != null) {
                syncCommands.select(DB_WORK_NUMBER);
                syncCommands.hincrby(KEY_VISITS, shortLink, 1);
                String domainName = Util.getDomainName(link);
                if (!"".equals(link)) {
                    syncCommands.hincrby(KEY_VISITS_BY_DOMAIN_ACTUAL, domainName, 1);
                    syncCommands.hincrby(KEY_VISITS_BY_DOMAIN_HISTORY, domainName, 1);
                }
            }
            return link;
        }
    }

    @Override
    public FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            if (!autorizedUser.getUserName().equals(owner)) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    if (!autorizedUser.isAdmin()) {
                        throw new RuntimeException(String.format("User '%s' does not have permissions to edit link", shortLink));
                    }
                }
            }
            String link = getLink(shortLink);
            String shortLinkWithContext = contextPath + shortLink;
            long ttl = syncCommands.ttl(shortLink);
            if (ttl < 0) {
                ttl = 0;
            }

            return new FullLink(shortLink, shortLinkWithContext,
                    link, "",
                    shortLinkWithContext + ".png", owner, ttl);
        }

    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, autorizedUser.getUserName())) {
                throw new RuntimeException(String.format("User '%s' is not exists",
                        autorizedUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(oldFullLink.getUserName())) {
                    throw new RuntimeException(String.format("User '%s' does not have permissions to update '%s'", autorizedUser, oldFullLink.getKey()));
                }

            }
            if (!syncCommands.hexists(KEY_USERS, newFullLink.getUserName())) {
                throw new RuntimeException(String.format("User '%s' is not exists",
                        newFullLink.getUserName()));
            }
            if (oldFullLink.getKey().equals(newFullLink.getKey())) {
                //days
                if (newFullLink.getSeconds() != oldFullLink.getSeconds()) {
                    syncCommands.select(DB_LINK_NUMBER);

                    if (newFullLink.getSeconds() == 0) {
                        syncCommands.persist(newFullLink.getKey());
                    } else {
                        syncCommands.expire(newFullLink.getKey(), newFullLink.getSeconds());
                    }
                }

                syncCommands.select(DB_WORK_NUMBER);
                //userName
                if (!newFullLink.getUserName().equals(oldFullLink.getUserName())) {
                    synchronized (redisClient) {
                        syncCommands.hdel(oldFullLink.getUserName(), oldFullLink.getKey());
                        syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), newFullLink
                                .getLink());
                    }
                } else {
                    //link
                    if (!newFullLink.getLink().equals(oldFullLink.getLink())) {
                        syncCommands.select(DB_LINK_NUMBER);
                        syncCommands.set(newFullLink.getKey(), newFullLink.getLink());
                        synchronized (redisClient) {
                            syncCommands.select(DB_WORK_NUMBER);
                            syncCommands.hdel(newFullLink.getUserName(), oldFullLink.getKey());
                            syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), newFullLink
                                    .getLink());
                        }
                    }
                }
            } else {
                syncCommands.select(DB_LINK_NUMBER);
                String existedKey = syncCommands.get(newFullLink.getKey());
                if (existedKey != null) {
                    throw new RuntimeException(String.format("Short link with key '%s' already exists" +
                                    ". Try another.",
                            newFullLink.getKey()));
                }

                synchronized (redisClient) {
                    syncCommands.rename(oldFullLink.getKey(), newFullLink.getKey());
                    syncCommands.set(newFullLink.getKey(), newFullLink.getLink());
                    if (newFullLink.getSeconds() == 0) {
                        syncCommands.persist(newFullLink.getKey());
                    } else {
                        syncCommands.expire(newFullLink.getKey(), newFullLink.getSeconds());
                    }
                    syncCommands.select(DB_WORK_NUMBER);
                    syncCommands.hdel(oldFullLink.getUserName(), oldFullLink.getKey());
                    syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), newFullLink
                            .getLink());
                }
            }
        }
    }

    @Override
    public long getLinkExpirePeriod(String shortLink) {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_LINK_NUMBER);
            long seconds = syncCommands.ttl(shortLink);
            if (seconds < 0) {
                seconds = 0;
            }

            return seconds;
        }
    }

    @Override
    public User getUser(User autorizedUser, String userName) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RuntimeException(String.format("User '%s' is not exists",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to edit" +
                                " users",
                        autorizedUser.getUserName()));

            }
            User user = null;
            try {
                user = new ObjectMapper().readValue(syncCommands.hget(KEY_USERS, userName), User
                        .class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return user;
        }
    }

    @Override
    public void updateUser(User autorizedUser, User newUser, User oldUser) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, oldUser.getUserName())) {
                throw new RuntimeException(String.format("User '%s' is not exists. Try another name",
                        oldUser.getUserName()));
            }
            if (!newUser.getUserName().equals(oldUser.getUserName()) &&
                    syncCommands.hexists(KEY_USERS, newUser.getUserName())) {
                throw new RuntimeException(String.format("User '%s' is already exists. Try another " +
                                "name",
                        newUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "update" +
                                " users",
                        autorizedUser.getUserName()));

            }
            synchronized (redisClient) {
                if (!newUser.getUserName().equals(oldUser.getUserName())) {
                    if (syncCommands.exists(oldUser.getUserName()) == 1) {
                        syncCommands.rename(oldUser.getUserName(), newUser.getUserName());
                    }
                    syncCommands.hdel(KEY_USERS, oldUser.getUserName());
                }
                String json = "";
                try {
                    json = new ObjectMapper().writeValueAsString(newUser);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                syncCommands.hset(KEY_USERS, newUser.getUserName(), json);
            }
        }
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RuntimeException(String.format("User '%s' is not exists. Try another name",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "delete" +
                                " users",
                        autorizedUser.getUserName()));

            }
            if (DEFAULT_USER.equals(userName) || ADMIN_USER.equals(userName)) {
                throw new RuntimeException(String.format("User '%s' is default user. Try another.",
                        userName));

            }
            if (syncCommands.exists(userName) == 1) {
                deleteAllUserLinksWithHistory(userName, syncCommands);
            }
            String json = syncCommands.hget(KEY_USERS, userName);
            syncCommands.select(DB_ARCHIVE_NUMBER);
            syncCommands.hset(KEY_USERS, userName, json);
            syncCommands.select(DB_WORK_NUMBER);
            syncCommands.hdel(KEY_USERS, userName);
        }
    }

    @Override
    public void clearUser(User autorizedUser, String userName) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RuntimeException(String.format("User '%s' is not exists. Try another name",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "delete" +
                                " users",
                        autorizedUser.getUserName()));

            }
            if (syncCommands.exists(userName) == 1) {
                deleteAllUserLinksWithHistory(userName, syncCommands);
            }
        }
    }

    private void deleteAllUserLinksWithHistory(String userName, RedisCommands<String, String> syncCommands) {
        syncCommands.hkeys(userName)
                .stream()
                .map(shortLink -> {
                    syncCommands.select(DB_WORK_NUMBER);
                    String link = syncCommands.hget(userName, shortLink);
                    decreaseVisits(shortLink, userName, link, syncCommands);
                    return shortLink;
                })
                .collect(Collectors.toList());
        syncCommands.del(userName);
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, owner)) {
                throw new RuntimeException(String.format("User '%s' is not exists. Try another name",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                    "watch user link",
                            autorizedUser.getUserName()));
                }
            }
            long size = syncCommands.hlen(owner);
            return size;
        }
    }

    @Override
    public long getUserArchiveSize(User autorizedUser, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);

            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                    "watch user archive links",
                            autorizedUser.getUserName()));
                }
            }
            long size = syncCommands.hlen(owner);
            return size;
        }
    }

    @Override
    public BigInteger checkFreeLinksDB() throws Exception {
        BigInteger addedKeys = BigInteger.ZERO;
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            String key = syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
            if (key != null) {
                syncCommands.select(DB_FREELINK_NUMBER);
                long size = syncCommands.dbsize();
                if (!"".equals(key)) {
                    if (size <= MIN_FREE_LINK_SIZE) {
                        addedKeys = updateFreeLinksDB(syncCommands, key);
                    }
                }
                return addedKeys;
            }
            throw new Exception(String.format("No '%s' in '%s'!", KEY_LENGTH, KEY_PREFERENCES));
        } catch (Exception e) {
            throw new Exception("Task: Check freelink DB. Cannot connect to databases!");
        }
    }

    @Override
    public BigInteger deleteExpiredUserLinks() throws Exception {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            List<String> links = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> links.add(key));
                cursor = syncCommands.scan(cursor);
            }
            List<String> deletedKeys = links
                    .stream()
                    .sorted()
                    .filter(user -> !user.startsWith("_"))
                    .map(user -> {
                        syncCommands.select(DB_WORK_NUMBER);
                        return syncCommands.hkeys(user)
                            .stream()
                            .map(shortLink -> {
                                syncCommands.select(DB_LINK_NUMBER);
                                String link = syncCommands.get(shortLink);
                                return new FullLink(shortLink, link, user);
                            })
                            .filter(fullLink -> fullLink.getLink() == null)
                            .collect(Collectors.toList());})
                    .flatMap(Collection::stream)
                    .map(fullLink -> {
                        String shortLink = fullLink.getKey();
                        syncCommands.select(DB_WORK_NUMBER);
                        String link = syncCommands.hget(fullLink.getUserName(), fullLink.getKey());
                        decreaseVisits(shortLink, fullLink.getUserName(), link, syncCommands);
                        return shortLink;
                    }).collect(Collectors.toList());

            return BigInteger.valueOf(deletedKeys.size());
        } catch (Exception e) {
            throw new Exception("Task: Delete expired links. Cannot connect to databases!");
        }
    }

    private BigInteger updateFreeLinksDB(RedisCommands<String, String> syncCommands, String key) {
        String[] keys = key.split("\\|");
        int maxKey = Arrays.stream(keys)
                .map(Integer::valueOf)
                .mapToInt(i -> i).max().getAsInt();

        int newKeyLength = maxKey + 1;
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, key + "|" + String.valueOf
                (newKeyLength));
        BigInteger addedKeys = createKeys(newKeyLength);
        return addedKeys;
    }

    @Override
    public BigInteger createKeys(int length) {
        BigInteger addedKeys = BigInteger.ZERO;
        FutureTask<BigInteger> futureTask = new FutureTask<>(() -> new KeyCreator().create(length));
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(futureTask);
        try {
            addedKeys = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return addedKeys;
    }

    public class KeyCreator {

        private final char[] alphabet =
                "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        .toCharArray();

        private RedisCommands<String, String> syncCommands;
        private BigInteger keyCount = BigInteger.ZERO;

        public BigInteger create(final int length) {

            try (StatefulRedisConnection<String, String> connection = connect()) {
                syncCommands = connection.sync();
                syncCommands.select(DB_FREELINK_NUMBER);
                char[] key = new char[length];
                recursive(key, 0, length);
                return keyCount;
            }
        }

        private void recursive(char[] key, int index, int length) {
            for (char anAlphabet : alphabet) {
                key[index] = anAlphabet;
                if (index == length - 1) {
                    addKey(String.valueOf(key));
                    keyCount = keyCount.add(BigInteger.ONE);
                } else {
                    int nextIndex = index + 1;
                    recursive(key, nextIndex, length);
                }
            }
        }

        private void addKey(String key) {
            syncCommands.set(key, "");
        }
    }
}
