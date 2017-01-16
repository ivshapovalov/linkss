package ru.ivan.linkss.repository;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RedisLinkRepositoryImpl implements LinkRepository {

    private static final int DB_WORK_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;

    private static final String KEY_USERS = "_users";
    private static final String KEY_LINKS = "_links";
    private static final String KEY_VISITS = "_visits";
    private static final String KEY_VISITS_BY_DOMAIN = "_visits_by_domain";
    private static final String KEY_PREFERENCES = "_preferences";
    private static final String KEY_LENGTH = "key.length";

    private static final String DEFAULT_USER = "user";
    private static final String DEFAULT_PASSWORD = "user";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admin";


    //private RedisClient redisClient = RedisClient.createShortLink("redis://localhost:6379/0");
//    private RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
////    private RedisClient redisClientStat = RedisClient.createShortLink
//            ("redis://h:p7c4dd823e40671d79ccbc943c29c2f0aec03d38cc29627b165cb1f32985fd766@ec2-54-221-228-237.compute-1.amazonaws.com:18689");
    //   private RedisClient redisClientByUsers = RedisClient.createShortLink
//            ("redis://h:p3291e9f52c34dafdb323e02b34803df7a3b56ac1f3c993dfe3215096fb76b154@ec2-184-72-246-90.compute-1.amazonaws.com:21279");
    private RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));

    public RedisLinkRepositoryImpl() {
//        StatefulRedisConnection<String, String> connection = redisClient.connect();
//        RedisCommands<String, String> syncCommands = connection.sync();
//
//        syncCommands.flushall();
//        syncCommands.select(DB_PREFERENCES_NUMBER);
//        syncCommands.set(KEY_LENGTH, String.valueOf("4"));
//        syncCommands.set(EXPIRE_PERIOD, "30");
//        syncCommands.select(DB_STATISTICS_NUMBER);
//        syncCommands.select(DB_FREELINK_NUMBER);
//        syncCommands.save();
//        connection.close();
    }

    public void init() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.flushall();
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, String.valueOf("4"));
        syncCommands.hset(KEY_USERS, ADMIN_USER, ADMIN_PASSWORD);
        syncCommands.hset(KEY_USERS, DEFAULT_USER, DEFAULT_PASSWORD);
        //syncCommands.hset(KEY_USERS, "USER", "USER");
        connection.close();
    }

    @Override
    public List<List<String>> getShortStat() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);
        Map<String, String> map = syncCommands.hscan(KEY_VISITS_BY_DOMAIN)
                .getMap();
        List<List<String>> shortStat = map.entrySet().stream()
                .map(p -> {
                    List<String> l = new ArrayList<>();
                    l.add(p.getKey());
                    l.add(String.valueOf(p.getValue()));
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
        Map<String, String> map = syncCommands.hscan(KEY_VISITS_BY_DOMAIN)
                .getMap();
        List<List<String>> shortStat = map.entrySet().stream()
                .map(p -> {
                    List<String> l = new ArrayList<>();
                    l.add(p.getKey());
                    l.add(String.valueOf(p.getValue()));
                    return l;
                }).collect(Collectors.toList());
        connection.close();

        return shortStat;
    }

    @Override
    public List<FullLink> getFullStat(String contextPath) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);

        List<FullLink> fullStat = new ArrayList<>();
        Map<String, Map<String, String>> collect = syncCommands.scan()
                .getKeys().stream().filter(u -> !u.startsWith("_"))
                .collect(Collectors.toMap(u -> u, u -> syncCommands.hscan(u).getMap()));
        for (Map.Entry<String, Map<String, String>> entry : collect.entrySet()) {
            for (Map.Entry<String, String> o : entry.getValue().entrySet()) {
                String shortLink = contextPath + o.getKey();
                String link = syncCommands.hget(KEY_LINKS, o.getKey());
                String visits = syncCommands.hget(KEY_VISITS, o.getKey());

                fullStat.add(new FullLink(o.getKey(), shortLink,
                        link, visits,
                        shortLink + ".png", entry.getKey()));
            }

        }

//                    String shortLink = contextPath + p.getKey();
//                    String link = syncCommands.hget(KEY_LINKS, p.getKey());
//                    return new FullLink(p.getKey(), shortLink,
//                            link, String.valueOf(p.getValue()),
//                            shortLink + ".png")
//                .collect(Collectors.toList());
//
//        List<FullLink> fullStat = map.entrySet().stream()
//                .map(p -> {
//                    String shortLink = contextPath + p.getKey();
//                    String link = syncCommands.hget(KEY_LINKS, p.getKey());
//                    return new FullLink(p.getKey(), shortLink,
//                            link, String.valueOf(p.getValue()),
//                            shortLink + ".png");
//                }).collect(Collectors.toList());
        connection.close();

        return fullStat;
    }

    @Override
    public List<FullLink> getFullStat(String userName, String contextPath) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);
        Map<String, String> map = syncCommands.hscan(userName)
                .getMap();
        List<FullLink> fullStat = map.entrySet().stream()
                .map(p -> {
                    String visits = syncCommands.hget(KEY_VISITS, p.getKey());
                    String shortLink = contextPath + p.getKey();
                    String link = syncCommands.hget(KEY_LINKS, p.getKey());
                    return new FullLink(p.getKey(), shortLink,
                            link, visits,
                            shortLink + ".png", userName);
                }).collect(Collectors.toList());
        connection.close();

        return fullStat;
    }

    @Override
    public String getRandomShortLink() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);
        String link = syncCommands.randomkey();
        connection.close();
        return link;
    }

    @Override
    public String createShortLink(String user, String link) {

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.select(DB_FREELINK_NUMBER);
        String shortLink = syncCommands.randomkey();
        if (shortLink == null) {
            return null;
        }
        syncCommands.del(shortLink);
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_LINKS, shortLink, link);
        if (user != null && !user.equals("")) {
            syncCommands.hset(user, shortLink, link);
        }
        syncCommands.hset(KEY_VISITS, shortLink, "0");

        String domainName = Util.getDomainName(link);
        if (!domainName.equals("")
                && (syncCommands.hget(KEY_VISITS_BY_DOMAIN, domainName) == null
        )) {
            syncCommands.hset(KEY_VISITS_BY_DOMAIN, domainName, "0");
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

        if (syncCommands.hexists(KEY_USERS, userName)) {
            throw new RuntimeException(String.format("User with name '%s' already exists. Try " +
                            "another name",
                    userName));
        }
        syncCommands.hset(KEY_USERS, userName, password);
        connection.close();
    }

    @Override
    public List<User> getUsers() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        List<User> users = syncCommands.hscan(KEY_USERS).getMap()
                .entrySet()
                .stream()
                .map(u -> new User(u.getKey(), u.getValue(), syncCommands.hlen(u.getKey())))
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
        String link = syncCommands.hget(KEY_LINKS, shortLink);
        if (link != null) {
            int visitsCount = Integer.valueOf(syncCommands.hget(KEY_VISITS, shortLink));
            syncCommands.hdel(KEY_VISITS, shortLink);
            syncCommands.hincrby(KEY_VISITS_BY_DOMAIN, Util.getDomainName(link), -1 * visitsCount);
            syncCommands.hdel(KEY_LINKS, shortLink);
            syncCommands.hdel(owner, shortLink);
        }
        connection.close();
    }

    @Override
    public String getLink(String shortLink) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_WORK_NUMBER);
        String link = syncCommands.hget(KEY_LINKS, shortLink);
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
        if (!newUser.getUserName().equals(oldUser.getUserName())) {
            if (syncCommands.exists(oldUser.getUserName()) == 1) {
                syncCommands.rename(oldUser.getUserName(), newUser.getUserName());
            }
            syncCommands.hdel(KEY_USERS, oldUser.getUserName());
        }
        syncCommands.hset(KEY_USERS, newUser.getUserName(), newUser.getPassword());

        connection.close();
    }

    @Override
    public void deleteUser(User autorizedUser, String userName) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
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

            syncCommands.hscan(userName)
                    .getMap()
                    .keySet()
                    .stream()
                    .map(shortLink-> {
                        int visitsCount = Integer.valueOf(syncCommands.hget(KEY_VISITS, shortLink));
                        syncCommands.hdel(KEY_VISITS,shortLink);
                        String link=syncCommands.hget(KEY_LINKS,shortLink);
                        syncCommands.hdel(KEY_LINKS, shortLink);
                        syncCommands.hincrby(KEY_VISITS_BY_DOMAIN,Util.getDomainName(link),
                                -1*visitsCount);
                        return shortLink;
                        })
                    .collect(Collectors.toList());
            syncCommands.del(userName);

        }
        syncCommands.hdel(KEY_USERS, userName);

        connection.close();
    }
}
