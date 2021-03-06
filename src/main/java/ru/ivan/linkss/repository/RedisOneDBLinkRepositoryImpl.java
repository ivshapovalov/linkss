package ru.ivan.linkss.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.ivan.linkss.repository.entity.*;
import ru.ivan.linkss.util.Constants;
import ru.ivan.linkss.util.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Repository
@Qualifier(value = "repositoryOne")
public class RedisOneDBLinkRepositoryImpl implements LinkRepository {

    private static final int DB_WORK_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;
    private static final int DB_LINK_NUMBER = 2;
    private static final int DB_ARCHIVE_NUMBER = 3;
    private static final int DB_VISITS_NUMBER = 4;
    private static final int DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER = 5;
    private static final int DB_VISITS_BY_DOMAIN_HISTORY_NUMBER = 6;

    private static final long MIN_FREE_LINK_SIZE = 10;
    private static final long SECONDS_IN_DAY = 3600 * 24;

    private static final String KEY_USERS = "_users";
    private static final String KEY_USERS_UNVERIFIED = "_users_unverified";
    private static final String KEY_PREFERENCES = "_preferences";
    private static final String KEY_LENGTH = "key.length";
    private static final String KEY_EXPIRATION_PERIOD = "expiration.period";

    private static final String PARAM_USER_AGENT = "user-agent";

    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "user";
    private static final String ADMIN_USER = "ivan";
    private static final String ADMIN_PASSWORD = "ivan";

    private volatile boolean freeLinkDBPopulatingInProgress = false;

    private final RedisClient redisClient;

    public RedisOneDBLinkRepositoryImpl() {
        this.redisClient = RedisClient.create(Constants.REDIS_URL);
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
                jAdmin = new User.Builder()
                        .addUserName(ADMIN_USER)
                        .addPassword(ADMIN_PASSWORD)
                        .addIsAdmin(true)
                        .addIsVerified(true).build().toJSON();
                jUser = new User.Builder()
                        .addUserName(DEFAULT_USER)
                        .addPassword(DEFAULT_PASSWORD)
                        .addIsAdmin(false)
                        .addIsVerified(true).build().toJSON();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            syncCommands.hset(KEY_USERS, ADMIN_USER, jAdmin);
            syncCommands.hset(KEY_USERS, DEFAULT_USER, jUser);
        }
    }

    public StatefulRedisConnection<String, String> connect() {
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
    public long getDomainsActualSize(User autorizedUser) throws RepositoryException {
        if (autorizedUser == null) {
            throw new RepositoryException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (!autorizedUser.isAdmin()) {
            throw new RepositoryException(String.format("User '%s' does not have permissions to see domains stat", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
            List<String> allKeys = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> allKeys.add(key));
                cursor = syncCommands.scan(cursor);
            }
            return allKeys.size();
        }
    }

    @Override
    public long getDomainsHistorySize(User autorizedUser) throws RepositoryException {
        if (autorizedUser == null) {
            throw new RepositoryException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (!autorizedUser.isAdmin()) {
            throw new RepositoryException(String.format("User '%s' does not have permissions to see domains stat", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
            List<String> allKeys = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> allKeys.add(key));
                cursor = syncCommands.scan(cursor);
            }
            return allKeys.size();
        }
    }

    @Override
    public long getUsersSize(User autorizedUser) throws RepositoryException {
        if (autorizedUser == null) {
            throw new RepositoryException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (!autorizedUser.isAdmin()) {
            throw new RepositoryException(String.format("User '%s' does not have permissions to see domains stat", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            long size = syncCommands.hlen(KEY_USERS);
            return size;
        }
    }

    @Override
    public long getVisitsByDomainActualSize(User autorizedUser) throws RepositoryException {
        if (autorizedUser == null) {
            throw new RepositoryException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (!autorizedUser.isAdmin()) {
            throw new RepositoryException(String.format("User '%s' does not have permissions to see " +
                    "visits", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
            List<String> keys = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> cursorKeys = cursor.getKeys();
                cursorKeys.stream().forEach(key -> keys.add(key));
                cursor = syncCommands.scan(cursor);
            }
            if (keys.size() == 0) {
                return 0;
            }

            long size = keys.stream()
                    .map(key -> syncCommands.hlen(key))
                    .reduce((sum, cost) -> sum + cost).get();
            return size;
        }
    }

    @Override
    public long getVisitsByDomainHistorySize(User autorizedUser) throws RepositoryException {
        if (autorizedUser == null) {
            throw new RepositoryException(String.format("User '%s' does not defined", autorizedUser.getUserName()));
        }
        if (!autorizedUser.isAdmin()) {
            throw new RepositoryException(String.format("User '%s' does not have permissions to see " +
                    "visits", autorizedUser.getUserName()));
        }
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
            List<String> keys = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> cursorKeys = cursor.getKeys();
                cursorKeys.stream().forEach(key -> keys.add(key));
                cursor = syncCommands.scan(cursor);
            }
            if (keys.size() == 0) {
                return 0;
            }

            long size = keys.stream()
                    .map(key -> syncCommands.hlen(key))
                    .reduce((sum, cost) -> sum + cost).get();
            return size;
        }
    }

    @Override
    public List<Domain> getShortStat(int offset, int recordsOnPage) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
            List<String> keys = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> cursorKeys = cursor.getKeys();
                cursorKeys.stream().forEach(key -> keys.add(key));
                cursor = syncCommands.scan(cursor);
            }

            List<Domain> shortStat = keys
                    .stream()
                    .map(domain -> {
                        syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
                        long sizeHistory = syncCommands.hlen(domain);
                        syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
                        long sizeActual = syncCommands.hlen(domain);
                        return new Domain(domain, sizeActual, sizeHistory)
                                ;
                    })
                    .sorted((d1, d2) -> (int) (d2.getVisitsActual()
                            - d1.getVisitsActual()))
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
                                    String jsonLink = syncCommands.get(shortLink);
                                    Link link = null;
                                    try {
                                        link = new Link().fromJSON(jsonLink);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String shortLinkWithContext = contextPath + shortLink;
                                    syncCommands.select(DB_WORK_NUMBER);
                                    String visits = String.valueOf(syncCommands.hlen(shortLink));
                                    return new FullLink.Builder()
                                            .addKey(shortLink)
                                            .addShortLink(shortLinkWithContext)
                                            .addLink(link.getLink())
                                            .addVisits(visits)
                                            .addImageLink(shortLinkWithContext + ".png")
                                            .addUserName(user)
                                            .addIpPosition(link.getIpPosition())
                                            .build();
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
                        String jsonLink = syncCommands.get(shortLink);
                        if (jsonLink != null) {
                            Link link = null;
                            try {
                                link = new Link().fromJSON(jsonLink);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String shortLinkWithContext = contextPath + shortLink;
                            syncCommands.select(DB_VISITS_NUMBER);
                            String visits = String.valueOf(syncCommands.hlen(shortLink));
                            syncCommands.select(DB_LINK_NUMBER);
                            long ttl = syncCommands.ttl(shortLink);
                            if (ttl < 0) {
                                return new FullLink.Builder()
                                        .addKey(shortLink)
                                        .addShortLink(shortLinkWithContext)
                                        .addLink(link.getLink())
                                        .addVisits(visits)
                                        .addImageLink(shortLinkWithContext + ".png")
                                        .addUserName(userName)
                                        .addIpPosition(link.getIpPosition())
                                        .build();
                            } else {
                                return new FullLink.Builder()
                                        .addKey(shortLink)
                                        .addShortLink(shortLinkWithContext)
                                        .addLink(link.getLink())
                                        .addVisits(visits)
                                        .addImageLink(shortLinkWithContext + ".png")
                                        .addUserName(userName)
                                        .addSeconds(ttl)
                                        .addIpPosition(link.getIpPosition())
                                        .build();
                            }
                        } else {
                            return null;
                        }
                    })
                    .filter(fullLink -> fullLink != null)
                    .filter(fullLink -> fullLink.getLink() != null)
                    .collect(Collectors.toList());
            return fullStat;
        }
    }

    @Override
    public List<Visit> getLinkVisits(User autorizedUser, String owner, String key, int offset, long
            recordsOnPage) throws RepositoryException {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), key)) {
                    throw new RepositoryException(String.format("User '%s' don't have link '%s'",
                            autorizedUser.getUserName(), key));
                }
            }
            syncCommands.select(DB_VISITS_NUMBER);
            List<Visit> visits = syncCommands.hkeys(key)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(time -> {
                        Visit visit = null;
                        try {
                            visit = new Visit().fromJSON(syncCommands.hget(key,
                                    time));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return visit;
                    })
                    .collect(Collectors.toList());
            return visits;
        }
    }

    @Override
    public List<Visit> getDomainActualVisits(User autorizedUser, String key, int offset,
                                             long
                                                     recordsOnPage) throws RepositoryException {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
            List<Visit> visits = syncCommands.hkeys(key)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(time -> {
                        Visit visit = null;
                        try {
                            visit = new Visit().fromJSON(syncCommands.hget(key,
                                    time));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return visit;
                    })
                    .collect(Collectors.toList());
            return visits;
        }
    }

    @Override
    public List<Visit> getDomainHistoryVisits(User autorizedUser, String key, int offset,
                                              long
                                                      recordsOnPage) throws RepositoryException {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
            List<Visit> visits = syncCommands.hkeys(key)
                    .stream()
                    .sorted()
                    .skip(offset)
                    .limit(recordsOnPage)
                    .map(time -> {
                        Visit visit = null;
                        try {
                            visit = new Visit().fromJSON(syncCommands.hget(key,
                                    time));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return visit;
                    })
                    .collect(Collectors.toList());
            return visits;
        }
    }

    @Override
    public List<Visit> getUserVisits(User autorizedUser, String owner) {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            List<Visit> visits = syncCommands.hkeys(owner)
                    .stream()
                    .sorted()
                    .map(key -> {
                        syncCommands.select(DB_VISITS_NUMBER);
                        return syncCommands.hkeys(key).stream()
                                .map(time -> {
                                    Visit visit = null;
                                    try {
                                        visit = new Visit().fromJSON(syncCommands.hget(key,
                                                time));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return visit;
                                })
                                .collect(Collectors.toList());
                    }).flatMap(Collection::stream)
                    .collect(Collectors.toList());
            return visits;
        }
    }

    @Override
    public List<Visit> getAllVisits() {

        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_VISITS_NUMBER);
            List<String> links = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            while (!cursor.isFinished()) {
                List<String> keys = cursor.getKeys();
                keys.stream().forEach(key -> links.add(key));
                cursor = syncCommands.scan(cursor);
            }

            List<Visit> visits = links.stream()
                    .sorted()
                    .map(key -> {
                        return syncCommands.hkeys(key).stream()
                                .map(time -> {
                                    Visit visit = null;
                                    try {
                                        visit = new Visit().fromJSON(syncCommands.hget(key,
                                                time));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return visit;
                                })
                                .collect(Collectors.toList());
                    }).flatMap(Collection::stream)
                    .collect(Collectors.toList());
            return visits;
        }
    }

    @Override
    public List<Link> getAllLinks() {

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

            return links.stream()
                    .sorted()
                    .map(key -> {
                        Link link = null;
                        try {
                            link = new Link().fromJSON(syncCommands.get(key));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return link;
                    })
                    .collect(Collectors.toList());
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
                            fullLink = new FullLink().fromJSON(syncCommands.hget(userName,
                                    shortLink));
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
    public String createShortLink(User autorizedUser, String link, IpPosition ipPosition) throws RepositoryException {
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
            String jsonLink = "";
            try {
                jsonLink = new Link.Builder().addKey(shortLink)
                        .addLink(link)
                        .addIpPosition(ipPosition).build()
                        .toJSON();
                syncCommands.set(shortLink, jsonLink);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            int days = 0;
            if (!"".equals(period)) {
                syncCommands.expire(shortLink, Integer.parseInt(period) * SECONDS_IN_DAY);
            }
            syncCommands.select(DB_WORK_NUMBER);

            if (autorizedUser != null && !"".equals(autorizedUser.getUserName())) {
                try {
                    syncCommands.hset(autorizedUser.getUserName(), shortLink, jsonLink);
                } catch (Exception e) {
                    throw new RepositoryException(String.format("Cannot add shortlink '%s' to users " +
                            "'%s' links", shortLink, autorizedUser.getUserName()));
                }
            } else {
                syncCommands.hset(DEFAULT_USER, shortLink, jsonLink);

            }
            return shortLink;
        }
    }

    @Override
    public void clear() {
        init();
    }

    @Override
    public void createUser(User user) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (user.getUserName().contains("_")) {
                throw new RepositoryException(String.format("User name '%s' contains symbol '_'. Try " +
                                "another name",
                        user.getUserName()));
            }

            synchronized (redisClient) {
                if (syncCommands.hexists(KEY_USERS, user.getUserName())) {
                    throw new RepositoryException(String.format("User with name '%s' already exists. Try " +
                                    "another name",
                            user.getUserName()));
                }
                String json = "";
                try {
                    json = user.toJSON();
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
                            user = new User().fromJSON(syncCommands.hget(KEY_USERS, userName));
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
    public User checkUser(User user) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, user.getUserName())) {
                throw new RepositoryException(String.format("User with name '%s' is not exists",
                        user.getUserName()));
            }


            User dbUser = null;
            try {
                dbUser = new User().fromJSON(syncCommands.hget(KEY_USERS, user
                        .getUserName()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!dbUser.isVerified()) {
                throw new RepositoryException(String.format("User with name '%s' not verified",
                        user.getUserName()));
            }
            if (!user.getPassword().equals(dbUser.getPassword())) {
                throw new RepositoryException(String.format("Password for user '%s' is " +
                                "wrong",
                        user.getUserName()));
            }
            return dbUser;
        }
    }

    @Override
    public String generateNewUUID(User user) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            do {
                final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                String userName = syncCommands.hget(KEY_USERS_UNVERIFIED, uuid);
                if (userName == null) {
                    syncCommands.hset(KEY_USERS_UNVERIFIED, uuid, user.getUserName());
                    return uuid;
                }
            } while (true);
        }
    }

    @Override
    public String getUserUUID(User user) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            do {
                String uuid = syncCommands.hkeys(KEY_USERS_UNVERIFIED).stream()
                        .filter(key -> {
                            String email = syncCommands.hget(KEY_USERS_UNVERIFIED, key);
                            if (email.equals(user.getUserName())
                                    ) {
                                return true;
                            } else {
                                return false;
                            }
                        }).findFirst().orElse(null);
                return uuid;
            } while (true);
        }
    }

    @Override
    public String verifyUser(String uuid) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            do {
                String userName = syncCommands.hget(KEY_USERS_UNVERIFIED, uuid);
                syncCommands.hdel(KEY_USERS_UNVERIFIED, uuid);
                if (userName != null) {
                    String jsonUser = syncCommands.hget(KEY_USERS, userName);
                    if (jsonUser != null && !"".equals(jsonUser)) {
                        try {
                            User user = new User().fromJSON(jsonUser);
                            user.setVerified(true);
                            syncCommands.hset(KEY_USERS, userName, user.toJSON());
                            return user.getUserName();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
                return null;
            } while (true);
        }
    }

    @Override
    public boolean checkLinkOwner(String key, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            String link = syncCommands.hget(owner, key);
            if (link == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void deleteLink(User autorizedUser, String shortLink, String owner) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, autorizedUser.getUserName())) {
                throw new RepositoryException(String.format("User '%s' is not exists",
                        autorizedUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RepositoryException(String.format("User '%s' does not have link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            syncCommands.select(DB_LINK_NUMBER);
            String link = syncCommands.get(shortLink);
            if (link != null) {
                moveLinkToArchive(shortLink, owner, link, syncCommands);
                syncCommands.select(DB_LINK_NUMBER);
                syncCommands.del(shortLink);
            }
        }
    }

    @Override
    public void deleteVisit(User autorizedUser, String owner, String key, String time) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), key)) {
                    throw new RepositoryException(String.format("User '%s' does not have link '%s'",
                            autorizedUser.getUserName(), key));
                }
            }
            syncCommands.select(DB_LINK_NUMBER);
            String jsonLink = syncCommands.get(key);
            Link link = null;
            try {
                link = new Link().fromJSON(jsonLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
            syncCommands.select(DB_VISITS_NUMBER);
            long visits = syncCommands.hdel(key, time);
            if (visits != 0 && link != null) {
                String domainName = Util.getDomainName(link.getLink());
                syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
                syncCommands.hdel(domainName, time);
            }
        }
    }

    @Override
    public void deleteArchiveLink(User autorizedUser, String shortLink, String owner) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);
            if (syncCommands.exists(owner) != 1) {
                throw new RepositoryException(String.format("Archive of user '%s' is not exists",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RepositoryException(String.format("User '%s' does not have archive " +
                                    "link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            syncCommands.hdel(owner, shortLink);
        }
    }

    @Override
    public void restoreArchiveLink(User autorizedUser, String shortLink, String owner) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);
            if (syncCommands.exists(owner) != 1) {
                throw new RepositoryException(String.format("Archive of user '%s' is not exists",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!syncCommands.hexists(autorizedUser.getUserName(), shortLink)) {
                    throw new RepositoryException(String.format("User '%s' does not have archive " +
                                    "link '%s'",
                            autorizedUser.getUserName(), shortLink));
                }
            }
            String jsonFullLink = syncCommands.hget(owner, shortLink);
            FullLink fullLink = null;
            String jsonLink = "";
            try {
                fullLink = new FullLink().fromJSON(jsonFullLink);
                Link link = new Link.Builder().addKey(shortLink).addLink(fullLink.getLink())
                        .addIpPosition(fullLink.getIpPosition()).build();
                jsonLink = link.toJSON();
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (redisClient) {
                syncCommands.select(DB_WORK_NUMBER);
                String jsonUser = syncCommands.hget(KEY_USERS, fullLink.getUserName());
                if (jsonUser == null) {
                    try {
                        jsonUser = new User.Builder()
                                .addUserName(fullLink.getUserName())
                                .addIsAdmin(false)
                                .addIsVerified(true).build().toJSON();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                syncCommands.hset(KEY_USERS, fullLink.getUserName(), jsonUser);
                syncCommands.hset(fullLink.getUserName(), fullLink.getKey(), jsonLink);

                syncCommands.select(DB_LINK_NUMBER);
                syncCommands.set(fullLink.getKey(), jsonLink);

                syncCommands.select(DB_ARCHIVE_NUMBER);
                syncCommands.hdel(owner, shortLink);

                syncCommands.select(DB_VISITS_NUMBER);
                String domainName = Util.getDomainName(fullLink.getLink());
                List<String> visits = syncCommands.hkeys(shortLink);
                visits.stream().forEach(time -> {
                    syncCommands.select(DB_VISITS_NUMBER);
                    String visit = syncCommands.hget(shortLink, time);
                    syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
                    syncCommands.hset(domainName, time, visit);
                });
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


    private void moveLinkToArchive(String shortLink, String owner, String jsonLink,
                                   RedisCommands<String,
                                           String> syncCommands) {
        synchronized (redisClient) {
            Link link = null;
            try {
                link = new Link().fromJSON(jsonLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String domainName = Util.getDomainName(link.getLink());
            syncCommands.select(DB_VISITS_NUMBER);
            List<String> times = syncCommands.hkeys(shortLink);
            syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
            times.stream().forEach(time -> {
                syncCommands.hdel(domainName, time);
            });

            FullLink fullLink = new FullLink.Builder()
                    .addKey(shortLink)
                    .addLink(link.getLink())
                    .addUserName(owner)
                    .addDeleted(LocalDateTime.now())
                    .addIpPosition(link.getIpPosition())
                    .build();
            syncCommands.select(DB_WORK_NUMBER);
            syncCommands.hdel(owner, shortLink);
            syncCommands.select(DB_ARCHIVE_NUMBER);
            String json = "";
            try {
                json = fullLink.toJSON();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            syncCommands.hset(owner, shortLink, json);
        }
    }

    @Override
    public Link getLink(String shortLink) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_LINK_NUMBER);
            String jsonLink = syncCommands.get(shortLink);
            Link link = null;
            try {
                link = new Link().fromJSON(jsonLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return link;
        }
    }

    @Override
    public String visitLink(String shortLink, IpPosition ipPosition, Map<String, String> params) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_LINK_NUMBER);
            String jsonLink = syncCommands.get(shortLink);
            Link link = null;
            try {
                link = new Link().fromJSON(jsonLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (jsonLink != null) {
                syncCommands.select(DB_VISITS_NUMBER);
                long time = Instant.now().toEpochMilli();
                String userAgent = params.get(PARAM_USER_AGENT);
                String json = "";
                try {
                    json = new Visit(time, ipPosition, userAgent).toJSON();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                syncCommands.hset(shortLink, String.valueOf(time), json);

                String domainName = Util.getDomainName(link.getLink());
                syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
                syncCommands.hset(domainName, String.valueOf(time), json);

                syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
                syncCommands.hset(domainName, String.valueOf(time), json);
            }
            return link.getLink();
        }
    }

    @Override
    public FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            if (!autorizedUser.getUserName().equals(owner)) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    if (!autorizedUser.isAdmin()) {
                        throw new RepositoryException(String.format("User '%s' does not have permissions to edit link", shortLink));
                    }
                }
            }
            Link link = getLink(shortLink);
            String shortLinkWithContext = contextPath + shortLink;
            long ttl = syncCommands.ttl(shortLink);
            if (ttl < 0) {
                ttl = 0;
            }

            return new FullLink.Builder()
                    .addKey(shortLink)
                    .addShortLink(shortLinkWithContext)
                    .addLink(link.getLink())
                    .addImageLink(shortLinkWithContext + ".png")
                    .addUserName(owner)
                    .addSeconds(ttl)
                    .addIpPosition(link.getIpPosition())
                    .build();
        }

    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, autorizedUser.getUserName())) {
                throw new RepositoryException(String.format("User '%s' is not exists",
                        autorizedUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(oldFullLink.getUserName())) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to update '%s'", autorizedUser, oldFullLink.getKey()));
                }

            }
            if (!syncCommands.hexists(KEY_USERS, newFullLink.getUserName())) {
                throw new RepositoryException(String.format("User '%s' is not exists",
                        newFullLink.getUserName()));
            }
            syncCommands.select(DB_WORK_NUMBER);
            String jsonLink = "";
            try {
                jsonLink = new Link.Builder().addKey(newFullLink.getKey())
                        .addLink(newFullLink
                                .getLink())
                        .addIpPosition(newFullLink.getIpPosition())
                        .build().toJSON();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
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

                //userName
                syncCommands.select(DB_WORK_NUMBER);
                if (!newFullLink.getUserName().equals(oldFullLink.getUserName())) {
                    synchronized (redisClient) {
                        syncCommands.hdel(oldFullLink.getUserName(), oldFullLink.getKey());
                        syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(),
                                jsonLink);
                    }
                } else {
                    //link
                    if (!newFullLink.getLink().equals(oldFullLink.getLink())) {
                        syncCommands.select(DB_LINK_NUMBER);
                        syncCommands.set(newFullLink.getKey(), jsonLink);
                        synchronized (redisClient) {
                            syncCommands.select(DB_WORK_NUMBER);
                            syncCommands.hdel(newFullLink.getUserName(), oldFullLink.getKey());
                            syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(),
                                    jsonLink);
                        }
                    }
                }
            } else {
                syncCommands.select(DB_LINK_NUMBER);
                String existedKey = syncCommands.get(newFullLink.getKey());
                if (existedKey != null) {
                    throw new RepositoryException(String.format("Short link with key '%s' already exists" +
                                    ". Try another.",
                            newFullLink.getKey()));
                }
                syncCommands.select(DB_FREELINK_NUMBER);
                String freeKey = syncCommands.get(newFullLink.getKey());
                if (freeKey == null) {
                    throw new RepositoryException(String.format("Short link with key '%s' already " +
                                    "exists in archive" +
                                    ". Try another.",
                            newFullLink.getKey()));
                }

                synchronized (redisClient) {
                    syncCommands.select(DB_LINK_NUMBER);
                    syncCommands.rename(oldFullLink.getKey(), newFullLink.getKey());
                    syncCommands.set(newFullLink.getKey(), jsonLink);
                    if (newFullLink.getSeconds() == 0) {
                        syncCommands.persist(newFullLink.getKey());
                    } else {
                        syncCommands.expire(newFullLink.getKey(), newFullLink.getSeconds());
                    }

                    syncCommands.select(DB_WORK_NUMBER);
                    syncCommands.hdel(oldFullLink.getUserName(), oldFullLink.getKey());
                    syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), jsonLink);

                    syncCommands.select(DB_VISITS_NUMBER);
                    if (syncCommands.exists(oldFullLink.getKey()) != 0) {
                        syncCommands.rename(oldFullLink.getKey(), newFullLink.getKey());
                    }

                    syncCommands.select(DB_FREELINK_NUMBER);
                    syncCommands.del(newFullLink.getKey());
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
    public User getUser(User autorizedUser, String userName) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RepositoryException(String.format("User '%s' is not exists",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(userName)) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to edit" +
                                    " users",
                            autorizedUser.getUserName()));
                }
            }
            User user = null;
            try {
                user = new User().fromJSON(syncCommands.hget(KEY_USERS, userName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return user;
        }
    }

    @Override
    public List<User> getUsers(String email) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);

            List<User> users = syncCommands.hkeys(KEY_USERS).stream()
                    .map(userName -> {
                        String jsonUser = syncCommands.hget(KEY_USERS, userName);
                        if (jsonUser != null && !"".equals(jsonUser)) {
                            try {
                                User user = new User().fromJSON(jsonUser);
                                return user;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }).filter(user -> email.equals(user.getEmail()))
                    .collect(Collectors.toList());
            return users;
        }
    }

    @Override
    public void updateUser(User autorizedUser, User newUser, User oldUser) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, oldUser.getUserName())) {
                throw new RepositoryException(String.format("User '%s' is not exists. Try another name",
                        oldUser.getUserName()));
            }
            if (!newUser.getUserName().equals(oldUser.getUserName()) &&
                    syncCommands.hexists(KEY_USERS, newUser.getUserName())) {
                throw new RepositoryException(String.format("User '%s' is already exists. Try another " +
                                "name",
                        newUser.getUserName()));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(oldUser.getUserName())) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                    "update users",
                            autorizedUser.getUserName()));
                }
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
                    json = newUser.toJSON();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                syncCommands.hset(KEY_USERS, newUser.getUserName(), json);
            }
        }
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RepositoryException(String.format("User '%s' is not exists. Try another name",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                "delete" +
                                " users",
                        autorizedUser.getUserName()));

            }
            if (DEFAULT_USER.equals(userName) || ADMIN_USER.equals(userName)) {
                throw new RepositoryException(String.format("User '%s' is default user. Try another.",
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
    public void clearUser(User autorizedUser, String userName) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, userName)) {
                throw new RepositoryException(String.format("User '%s' is not exists. Try another name",
                        userName));
            }
            if (!autorizedUser.isAdmin()) {
                throw new RepositoryException(String.format("User '%s' does not have permissions to " +
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
                    String jsonLink = syncCommands.hget(userName, shortLink);
                    moveLinkToArchive(shortLink, userName, jsonLink, syncCommands);
                    return shortLink;
                })
                .collect(Collectors.toList());
        syncCommands.del(userName);
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_WORK_NUMBER);
            if (!syncCommands.hexists(KEY_USERS, owner)) {
                throw new RepositoryException(String.format("User '%s' is not exists. Try another name",
                        owner));
            }
            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                    "watch user link",
                            autorizedUser.getUserName()));
                }
            }
            long size = syncCommands.hlen(owner);
            return size;
        }
    }

    @Override
    public long getUserArchiveSize(User autorizedUser, String owner) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_ARCHIVE_NUMBER);

            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                    "watch user archive links",
                            autorizedUser.getUserName()));
                }
            }
            long size = syncCommands.hlen(owner);
            return size;
        }
    }

    @Override
    public long getLinkVisitsSize(User autorizedUser, String owner, String key) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_VISITS_NUMBER);

            if (!autorizedUser.isAdmin()) {
                if (!autorizedUser.getUserName().equals(owner)) {
                    throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                    "watch user visits",
                            autorizedUser.getUserName()));
                }
            }
            long size = syncCommands.hlen(key);
            return size;
        }
    }

    @Override
    public long getDomainActualVisitsSize(User autorizedUser, String key) throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_VISITS_BY_DOMAIN_ACTUAL_NUMBER);
            if (!autorizedUser.isAdmin()) {
                throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                "watch domains visits",
                        autorizedUser.getUserName()));

            }
            long size = syncCommands.hlen(key);
            return size;
        }
    }

    @Override
    public long getDomainHistoryVisitsSize(User autorizedUser, String key) throws
            RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            syncCommands.select(DB_VISITS_BY_DOMAIN_HISTORY_NUMBER);
            if (!autorizedUser.isAdmin()) {
                throw new RepositoryException(String.format("User '%s' does not have permissions to " +
                                "watch domains visits",
                        autorizedUser.getUserName()));

            }
            long size = syncCommands.hlen(key);
            return size;
        }
    }

    @Override
    public long getUserVisitsSize(User autorizedUser, String owner) {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);

            List<Visit> visits = getUserVisits(autorizedUser, owner);
            long size = visits.size();
            return size;
        }
    }

    @Override
    public BigInteger checkFreeLinksDB() throws RepositoryException {
        BigInteger addedKeys = BigInteger.ZERO;
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();

            boolean needToAdd = false;
            int newKeyLength = 0;
            if (freeLinkDBPopulatingInProgress) {
                throw new RepositoryException("Task: Check freelink DB. Updating keys in process!");
            }
            synchronized (redisClient) {
                syncCommands.select(DB_WORK_NUMBER);
                String key = syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
                if (key != null) {
                    syncCommands.select(DB_FREELINK_NUMBER);
                    long size = syncCommands.dbsize();
                    if (!"".equals(key)) {
                        if (size <= MIN_FREE_LINK_SIZE) {
                            String[] keys = key.split("\\|");
                            int maxKey = Arrays.stream(keys)
                                    .map(Integer::valueOf)
                                    .mapToInt(i -> i).max().getAsInt();

                            newKeyLength = maxKey + 1;
                            syncCommands.select(DB_WORK_NUMBER);
                            syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, key + "|" + String.valueOf
                                    (newKeyLength));
                            freeLinkDBPopulatingInProgress = true;
                            needToAdd = true;
                        }
                    }
                } else {
                    throw new RepositoryException(String.format("No '%s' in '%s'!", KEY_LENGTH, KEY_PREFERENCES));
                }
            }
            if (needToAdd) {
                addedKeys = updateFreeLinksDB(syncCommands, newKeyLength);
                freeLinkDBPopulatingInProgress = false;
            }
            return addedKeys;

        } catch (Exception e) {
            throw new RepositoryException("Task: Check freelink DB. Cannot connect to databases!");
        }
    }

    @Override
    public BigInteger deleteExpiredUserLinks() throws RepositoryException {
        try (StatefulRedisConnection<String, String> connection = connect()) {
            RedisCommands<String, String> syncCommands = connection.sync();
            syncCommands.select(DB_WORK_NUMBER);
            List<String> links = new ArrayList<>();
            KeyScanCursor cursor = syncCommands.scan();
            cursor.setFinished(false);
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
                                    String jsonLink = syncCommands.get(shortLink);
                                    Link link = null;
                                    try {
                                        link = new Link().fromJSON(jsonLink);
                                    } catch (Exception e) {
                                        return new FullLink.Builder()
                                                .addKey(shortLink)
                                                .addUserName(user)
                                                .build();
                                    }
                                    return new FullLink.Builder()
                                            .addKey(shortLink)
                                            .addLink(link.getLink())
                                            .addUserName(user)
                                            .addIpPosition(link.getIpPosition())
                                            .build();
                                })
                                .filter(fullLink -> fullLink.getLink() == null)
                                .collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .map(fullLink -> {
                        String shortLink = fullLink.getKey();
                        syncCommands.select(DB_WORK_NUMBER);
                        String jsonLink = syncCommands.hget(fullLink.getUserName(), fullLink.getKey
                                ());
                        moveLinkToArchive(shortLink, fullLink.getUserName(), jsonLink,
                                syncCommands);
                        return shortLink;
                    }).collect(Collectors.toList());

            return BigInteger.valueOf(deletedKeys.size());
        } catch (Exception e) {
            throw new RepositoryException("Task: Delete expired links. Cannot connect to database!");
        }
    }

    private BigInteger updateFreeLinksDB(RedisCommands<String, String> syncCommands, int
            newKeyLength) {
        syncCommands.select(DB_WORK_NUMBER);
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
        //62
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
            syncCommands.select(DB_FREELINK_NUMBER);
            syncCommands.set(key, "");
        }
    }
}
