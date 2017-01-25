package ru.ivan.linkss.repository;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.util.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

@Component
@Qualifier(value = "repositoryTwo")
public class RedisTwoDBLinkRepositoryImpl implements LinkRepository {

    //REDIS 1
    private static final int DB_WORK_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;
    //REDIS 2
    private static final int DB_LINK_NUMBER = 0;

    private static final long MIN_FREE_LINK_SIZE = 10;
    private static final long SECONDS_IN_DAY = 3600 * 24;

    private static final String KEY_USERS = "_users";
    private static final String KEY_VISITS = "_visits";
    private static final String KEY_VISITS_BY_DOMAIN = "_visits_by_domain";
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
    private final RedisClient redisClientLinks;

    public RedisTwoDBLinkRepositoryImpl() {
        this.redisClient = RedisClient.create(System.getenv("REDIS_URL"));
        this.redisClientLinks = RedisClient.create(System.getenv
                ("HEROKU_REDIS_AMBER_URL"));
    }

    public RedisTwoDBLinkRepositoryImpl(RedisClient redisClient, RedisClient redisClientLinks) {
        this.redisClient = redisClient;
        this.redisClientLinks = redisClientLinks;
    }

    @Override
    public void init() {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.flushall();
        syncCommandsLinks.flushall();
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, String.valueOf("1"));
        createKeys(1);
        syncCommands.hset(KEY_PREFERENCES, KEY_EXPIRATION_PERIOD, String.valueOf("30"));
        syncCommands.hset(KEY_USERS, ADMIN_USER, ADMIN_PASSWORD);
        syncCommands.hset(KEY_USERS, DEFAULT_USER, DEFAULT_PASSWORD);
        connectionLinks.close();
        connection.close();
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

    private StatefulRedisConnection<String, String> connectLinks() {
        boolean connectionFailed = true;
        StatefulRedisConnection<String, String> connection = null;
        do {
            try {
                connection = redisClientLinks.connect();
                connectionFailed = false;
            } catch (Exception e) {
                connectionFailed = true;
            }
        } while (connectionFailed);
        return connection;
    }

    @Override
    public long getDBLinksSize() {
        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        syncCommandsLinks.select(DB_LINK_NUMBER);
        long size = syncCommandsLinks.dbsize();
        connectionLinks.close();
        return size;
    }

    @Override
    public long getDBFreeLinksSize() {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_FREELINK_NUMBER);
        long size = syncCommands.dbsize();
        connection.close();
        return size;
    }

    @Override
    public List<List<String>> getShortStat() {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);
        List<List<String>> shortStat = syncCommands.hkeys(KEY_VISITS_BY_DOMAIN)
                .stream()
                .map(domain -> {
                    List<String> l = new ArrayList<>();
                    String count = syncCommands.hget(KEY_VISITS_BY_DOMAIN, domain);
                    l.add(domain);
                    l.add(count);
                    return l;
                }).collect(Collectors.toList());
        connection.close();
        return shortStat;
    }

    @Override
    public List<FullLink> getFullStat(String contextPath) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
        List<FullLink> fullStat = syncCommands.keys("*")
                .stream()
                .filter(user -> !user.startsWith("_"))
                .map(user -> syncCommands.hkeys(user)
                        .stream()
                        .map(shortLink -> {
                            String link = syncCommandsLinks.get(shortLink);
                            String shortLinkWithContext = contextPath + shortLink;
                            String visits = syncCommands.hget(KEY_VISITS, shortLink);
                            return new FullLink(shortLink, shortLinkWithContext,
                                    link, visits,
                                    shortLinkWithContext + ".png", user);
                        }).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        connectionLinks.close();
        connection.close();
        return fullStat;
    }

    @Override
    public List<FullLink> getFullStat(String userName, String contextPath, int offset, int recordsOnPage) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
        List<FullLink> fullStat = syncCommands.hkeys(userName)
                .stream()
                .sorted()
                .skip(offset)
                .limit(recordsOnPage)
                .map(shortLink -> {
                    String link = syncCommandsLinks.get(shortLink);
                    String shortLinkWithContext = contextPath + shortLink;
                    String visits = syncCommands.hget(KEY_VISITS, shortLink);
                    long pttl = syncCommandsLinks.pttl(shortLink);
                    if (pttl < 0) {
                        return new FullLink(shortLink, shortLinkWithContext,
                                link, visits,
                                shortLinkWithContext + ".png", userName);
                    } else {
                        return new FullLink(shortLink, shortLinkWithContext,
                                link, visits,
                                shortLinkWithContext + ".png", userName, pttl / 1000 / SECONDS_IN_DAY+1);
                    }

                }).collect(Collectors.toList());
        connectionLinks.close();
        connection.close();
        return fullStat;
    }


    @Override
    public String getRandomShortLink() {
        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        syncCommandsLinks.select(DB_LINK_NUMBER);
        String shortLink = syncCommandsLinks.randomkey();
        connectionLinks.close();
        return shortLink;
    }

    @Override
    public String createShortLink(String user, String link) {

        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        String period = syncCommands.hget(KEY_PREFERENCES, KEY_EXPIRATION_PERIOD);
        syncCommands.select(DB_FREELINK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
        String shortLink = null;

        synchronized (redisClient) {
            boolean failed = false;
            do {
                shortLink = syncCommands.randomkey();
                //shortLink = link;
                failed = shortLink == null;
                syncCommands.del(shortLink);
            } while (failed);
        }

        syncCommandsLinks.set(shortLink, link);
        int days = 0;
        if (period != null && !"".equals(period)) {
            syncCommandsLinks.expire(shortLink, Integer.parseInt(period) * SECONDS_IN_DAY);
        }
        syncCommands.select(DB_WORK_NUMBER);

        if (user != null && !user.equals("")) {
            syncCommands.hset(user, shortLink, link);
        }
        syncCommands.hset(KEY_VISITS, shortLink, "0");
        String domainName = "";

        domainName = Util.getDomainName(link);
        synchronized (redisClient) {
            if (!domainName.equals("")
                    && (syncCommands.hget(KEY_VISITS_BY_DOMAIN, domainName) == null
            )) {
                syncCommands.hset(KEY_VISITS_BY_DOMAIN, domainName, "0");
            }
        }
        connectionLinks.close();
        connection.close();
        return shortLink;
    }

    @Override
    public void createUser(String userName, String password) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        if (userName.contains("_")) {
            throw new RuntimeException(String.format("User name '%s' contains symbol '_'. Try " +
                            "another name",
                    userName));
        }

        synchronized (redisClient) {
            if (syncCommands.hexists(KEY_USERS, userName)) {
                throw new RuntimeException(String.format("User with name '%s' already exists. Try " +
                                "another name",
                        userName));
            }
            syncCommands.hset(KEY_USERS, userName, password);
        }
        connection.close();
    }

    @Override
    public List<User> getUsers() {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        List<User> users = syncCommands.hkeys(KEY_USERS)
                .stream()
                .map(userName -> {
                    String password = syncCommands.hget(KEY_USERS, userName);
                    return new User(userName, password, syncCommands.hlen(userName));
                })
                .collect(Collectors.toList());
        connection.close();
        return users;
    }

    @Override
    public boolean checkUser(User user) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        if (!syncCommands.hexists(KEY_USERS, user.getUserName())) {
            throw new RuntimeException(String.format("User with name '%s' is not exists",
                    user.getUserName()));
        }
        if (!syncCommands.hget(KEY_USERS, user.getUserName()).equals(user.getPassword())) {
            throw new RuntimeException(String.format("Password for user '%s' is " +
                            "wrong",
                    user.getUserName()));
        }
        connection.close();
        return true;
    }

    @Override
    public void deleteUserLink(User autorizedUser, String shortLink, String owner) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
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
        String link = syncCommandsLinks.get(shortLink);
        if (link != null) {
            synchronized (redisClient) {
                synchronized (redisClientLinks) {
                    syncCommands.hdel(KEY_VISITS, shortLink);
                    syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link), -1 * Integer.parseInt(syncCommands.hget(KEY_VISITS, shortLink)));
                    syncCommandsLinks.del(shortLink);
                    syncCommands.hdel(owner, shortLink);
                }
            }
        }
        connectionLinks.close();
        connection.close();
    }

    @Override
    public void updateUserLinkDays(User autorizedUser, String shortLink, String owner, long days) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
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
        String link = syncCommandsLinks.get(shortLink);
        if (link != null) {
            synchronized (redisClient) {
                synchronized (redisClientLinks) {
                    syncCommandsLinks.expire(shortLink, days * SECONDS_IN_DAY);
                }
            }
        }
        connectionLinks.close();
        connection.close();
    }

    @Override
    public String getLink(String shortLink) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);

        String link = syncCommandsLinks.get(shortLink);
        syncCommands.hincrby(KEY_VISITS, shortLink, 1);
        if (link != null && !link.equals("")) {
            syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link), 1);
        }

        connectionLinks.close();
        connection.close();
        return link;
    }

    @Override
    public FullLink getFullLink(User autorizedUser, String shortLink, String owner, String contextPath) {
        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        if (!autorizedUser.getUserName().equals(owner)) {
            if (!autorizedUser.isAdmin()) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to edit link", shortLink));
            }
        }

        String link = getLink(shortLink);
        String shortLinkWithContext = contextPath + shortLink;
        long pttl = syncCommandsLinks.pttl(shortLink) / 1000 / SECONDS_IN_DAY;
        if (pttl < 0) {
            pttl = 0;
        }

        connectionLinks.close();
        return new FullLink(shortLink, shortLinkWithContext,
                link, "",
                shortLinkWithContext + ".png", owner, pttl);

    }

    @Override
    public void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
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
            if (newFullLink.getDays() != oldFullLink.getDays()) {
                if (newFullLink.getDays() == 0) {
                    syncCommandsLinks.persist(newFullLink.getKey());
                } else {
                    syncCommandsLinks.expire(newFullLink.getKey(), newFullLink.getDays() * SECONDS_IN_DAY+1);
                }
            }

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
                    syncCommandsLinks.set(newFullLink.getKey(), newFullLink.getLink());
                    synchronized (redisClient) {
                        syncCommands.hdel(newFullLink.getUserName(), oldFullLink.getKey());
                        syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), newFullLink
                                .getLink());
                    }
                }
            }
        } else {
            String existedKey = syncCommandsLinks.get(newFullLink.getKey());
            if (existedKey != null) {
                throw new RuntimeException(String.format("Short link with key '%s' already exists" +
                                ". Try another.",
                        newFullLink.getKey()));
            }

            synchronized (redisClientLinks) {
                syncCommandsLinks.del(oldFullLink.getKey());
                syncCommandsLinks.set(newFullLink.getKey(), newFullLink.getLink());
                if (newFullLink.getDays() == 0) {
                    syncCommandsLinks.persist(newFullLink.getKey());
                } else {
                    syncCommandsLinks.expire(newFullLink.getKey(), newFullLink.getDays() * SECONDS_IN_DAY);
                }
            }
            synchronized (redisClient) {
                syncCommands.hdel(oldFullLink.getUserName(), oldFullLink.getKey());
                syncCommands.hset(newFullLink.getUserName(), newFullLink.getKey(), newFullLink
                        .getLink());
            }

        }

        connectionLinks.close();
        connection.close();
    }

    @Override
    public long getLinkDays(String shortLink) {

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommandsLinks.select(DB_LINK_NUMBER);
        long days = syncCommandsLinks.pttl(shortLink) / 1000 / SECONDS_IN_DAY+1;

        connectionLinks.close();
        return days;
    }

    @Override
    public User getUser(User autorizedUser, String userName) {
        StatefulRedisConnection<String, String> connection = connect();
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
        String password = syncCommands.hget(KEY_USERS, userName);
        User user = new User(userName, password);
        connection.close();
        return user;
    }

    @Override
    public void updateUser(User autorizedUser, User newUser, User oldUser) {
        StatefulRedisConnection<String, String> connection = connect();
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
            syncCommands.hset(KEY_USERS, newUser.getUserName(), newUser.getPassword());
        }
        connection.close();
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = connectLinks();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.select(DB_LINK_NUMBER);
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
            syncCommands.hkeys(userName)
                    .stream()
                    .map(shortLink -> {
                        synchronized (redisClient) {
                            int visitsCount = Integer.valueOf(syncCommands.hget(KEY_VISITS, shortLink));
                            syncCommands.hdel(KEY_VISITS, shortLink);
                            String link = syncCommandsLinks.get(shortLink);
                            syncCommandsLinks.del(shortLink);
                            syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link),
                                    -1 * visitsCount);
                            return shortLink;
                        }
                    })
                    .collect(Collectors.toList());
            syncCommands.del(userName);

        }
        syncCommands.hdel(KEY_USERS, userName);

        connectionLinks.close();
        connection.close();
    }

    @Override
    public long getUserLinksSize(User autorizedUser, String owner) {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        if (!syncCommands.hexists(KEY_USERS, owner)) {
            throw new RuntimeException(String.format("User '%s' is not exists. Try another name",
                    owner));
        }
//        if (syncCommands.exists(owner) != 1) {
////            throw new RuntimeException(String.format("User '%s' do not have links. Try another " +
////                            "name",
////                    owner))
//
//                    ;
//        }
        if (!autorizedUser.isAdmin()) {
            if (!autorizedUser.getUserName().equals(owner)) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "watch user link",
                        autorizedUser.getUserName()));
            }
        }
        long size = syncCommands.hlen(owner);
        connection.close();
        return size;
    }

    @Override
    public BigInteger checkFreeLinksDB() {
        StatefulRedisConnection<String, String> connection = connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        String key = syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
        syncCommands.select(DB_FREELINK_NUMBER);
        long size = syncCommands.dbsize();
        BigInteger addedKeys = BigInteger.ZERO;
        if (key != null && !"".equals(key)) {
            if (size <= MIN_FREE_LINK_SIZE) {
                addedKeys = updateFreeLinksDB(syncCommands, key);
            }
        }
        connection.close();
        return addedKeys;
    }

    private BigInteger updateFreeLinksDB(RedisCommands<String, String> syncCommands, String key) {
        BigInteger addedKeys = BigInteger.ZERO;
        String[] keys = key.split("\\|");
        int maxKey = Arrays.stream(keys)
                .map(Integer::valueOf)
                .mapToInt(i -> i).max().getAsInt();

        int newKeyLength = maxKey + 1;
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, key + "|" + String.valueOf
                (newKeyLength));
        addedKeys = createKeys(newKeyLength);
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

        private StatefulRedisConnection<String, String> connection;
        private RedisCommands<String, String> syncCommands;
        private BigInteger keyCount = BigInteger.ZERO;

        public BigInteger create(final int length) {

//        redisClient = RedisClient.create
//                ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
            connection = connect();
            syncCommands = connection.sync();
            syncCommands.select(DB_FREELINK_NUMBER);

            char[] key = new char[length];
            recursive(key, 0, length);

            connection.close();
            return keyCount;
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
