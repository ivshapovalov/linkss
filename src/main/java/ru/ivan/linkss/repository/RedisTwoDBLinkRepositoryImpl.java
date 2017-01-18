package ru.ivan.linkss.repository;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.service.KeyCreator;
import ru.ivan.linkss.util.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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
    private RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));
    private RedisClient redisClientLinks = RedisClient.create(System.getenv
            ("HEROKU_REDIS_AMBER_URL"));

    public RedisTwoDBLinkRepositoryImpl() {
    }

    public void init() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.flushall();
        syncCommandsLinks.flushall();
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, String.valueOf("1"));
        syncCommands.hset(KEY_PREFERENCES, KEY_EXPIRATION_PERIOD, String.valueOf("30"));
        syncCommands.hset(KEY_USERS, ADMIN_USER, ADMIN_PASSWORD);
        syncCommands.hset(KEY_USERS, DEFAULT_USER, DEFAULT_PASSWORD);
        connection.close();
    }

    @Override
    public List<List<String>> getShortStat() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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
    public List<List<String>> getShortStat(String autorizedUser) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);

        List<FullLink> fullStat = syncCommands.keys("*")
                .stream()
                .filter(user -> !user.startsWith("_"))
                .map(user -> {
                    List<FullLink> links = syncCommands.hkeys(user)
                            .stream()
                            .map(shortLink -> {
                                String link = syncCommandsLinks.get(shortLink);
                                String shortLinkWithContext = contextPath + shortLink;
                                String visits = syncCommands.hget(KEY_VISITS, shortLink);
                                return new FullLink(shortLink, shortLinkWithContext,
                                        link, visits,
                                        shortLinkWithContext + ".png", user);
                            }).collect(Collectors.toList());

                    return links;
                })
                .flatMap(links -> links.stream())
                .collect(Collectors.toList());

        connection.close();
        return fullStat;
    }

    @Override
    public List<FullLink> getFullStat(String userName, String contextPath) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);

        List<FullLink> fullStat = syncCommands.hkeys(userName)
                .stream()
                .map(shortLink -> {
                    String link = syncCommandsLinks.get(shortLink);
                    String shortLinkWithContext = contextPath + shortLink;
                    String visits = syncCommands.hget(KEY_VISITS, shortLink);
                    return new FullLink(shortLink, shortLinkWithContext,
                            link, visits,
                            shortLinkWithContext + ".png", userName);
                }).collect(Collectors.toList());
        connection.close();
        return fullStat;
    }

    @Override
    public String getRandomShortLink() {
        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();
        syncCommandsLinks.select(DB_LINK_NUMBER);
        String shortLink = syncCommandsLinks.randomkey();
        connectionLinks.close();
        return shortLink;
    }

    @Override
    public String createShortLink(String user, String link) {

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_FREELINK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);
        String shortLink = null;
        //synchronized (redisClient) {
        boolean failed = true;
        do {
            shortLink = syncCommands.randomkey();
            syncCommands.watch(shortLink);
            syncCommands.multi();
            if (shortLink == null) {
                return null;
            }
            syncCommands.del(shortLink);
            failed = syncCommands.exec().wasRolledBack();
        } while (failed);
        //}

        syncCommandsLinks.set(shortLink, link);
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
        connection.close();
        return shortLink;
    }

    @Override
    public void createUser(String userName, String password) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        if (userName.contains("_")) {
            throw new RuntimeException(String.format("User name '%s' contains symbol '_'. Try " +
                            "another name",
                    userName));
        }

        boolean failed = false;
        do {
            syncCommands.multi();
            if (syncCommands.hexists(KEY_USERS, userName)) {
                throw new RuntimeException(String.format("User with name '%s' already exists. Try " +
                                "another name",
                        userName));
            }
            syncCommands.hset(KEY_USERS, userName, password);
            failed = syncCommands.exec().wasRolledBack();
        } while (failed);
        connection.close();
    }

    @Override
    public List<User> getUsers() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        List<User> users = syncCommands.hkeys(KEY_USERS)
                .stream()
                .map(userName -> {
                    String password = syncCommands.hget(KEY_USERS, userName);
                    User user = new User(userName, password, syncCommands.hlen(userName));
                    return user;
                })
                .collect(Collectors.toList());
        connection.close();
        return users;
    }

    @Override
    public boolean checkUser(User user) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
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
                    int visitsCount = Integer.valueOf(syncCommands.hget(KEY_VISITS, shortLink));
                    syncCommands.hdel(KEY_VISITS, shortLink);
                    syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link), -1 * visitsCount);
                    syncCommandsLinks.del(shortLink);
                    syncCommands.hdel(owner, shortLink);
                }
            }
        }
        connection.close();
    }

    @Override
    public String getLink(String shortLink) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
        RedisCommands<String, String> syncCommandsLinks = connectionLinks.sync();

        syncCommands.select(DB_WORK_NUMBER);
        syncCommandsLinks.select(DB_LINK_NUMBER);

        String link = syncCommandsLinks.get(shortLink);
        syncCommands.hincrby(KEY_VISITS, shortLink, 1);
        syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link), 1);

        connection.close();
        return link;
    }

    @Override
    public User getUser(User autorizedUser, String userName) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        StatefulRedisConnection<String, String> connectionLinks = redisClientLinks.connect();
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

        connection.close();
    }

    @Override
    public BigInteger updateFreeLinks() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_WORK_NUMBER);
        String key = syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
        syncCommands.select(DB_FREELINK_NUMBER);
        long size = syncCommands.dbsize();
        BigInteger addedKeys = BigInteger.ZERO;
        if (size <= MIN_FREE_LINK_SIZE) {
            String[] keys = key.split("\\|");
            int maxKey = Arrays.asList(keys).stream()
                    .map(p -> Integer.valueOf(p))
                    .mapToInt(i -> i).max().getAsInt();

            int newKeyLength = maxKey + 1;
            syncCommands.select(DB_WORK_NUMBER);
            boolean updated = syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, key + "|" + String.valueOf
                    (newKeyLength));
            FutureTask<BigInteger> futureTask = new FutureTask<>(() -> new KeyCreator().create(newKeyLength));
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(futureTask);
            try {
                addedKeys = futureTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        connection.close();
        return addedKeys;
    }
}
