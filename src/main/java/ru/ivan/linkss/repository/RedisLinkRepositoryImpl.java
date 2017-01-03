package ru.ivan.linkss.repository;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import ru.ivan.linkss.util.Util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Ivan on 01.01.2017.
 */
public class RedisLinkRepositoryImpl implements LinkRepository {

    private static final int DB_LINK_NUMBER = 0;
    private static final int DB_PREFERENCES_NUMBER = 2;
    private static final int DB_FREELINK_NUMBER = 1;
    private static final int DB_STATISTICS_NUMBER = 3;

    private RedisClient redisClient = RedisClient.create("redis://localhost:6379/0");
    private long dayInSeconds = 60 * 60 * 24;

    public RedisLinkRepositoryImpl() {
    }

    public void init() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.flushall();
        syncCommands.select(DB_PREFERENCES_NUMBER);
        syncCommands.set("key.length", String.valueOf("2,3,4,5,6"));
        syncCommands.set("expire.period", "30");
        syncCommands.save();
        connection.close();
        //redisClient.shutdown();
    }

    @Override
    public String getRandomShortLink() {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_LINK_NUMBER);
        String link = syncCommands.randomkey();
        connection.close();
        return link;
    }

    @Override
    public String create(String link) {

        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_FREELINK_NUMBER);
        String shortLink = syncCommands.randomkey();
        syncCommands.del(shortLink);
        syncCommands.select(DB_LINK_NUMBER);
        syncCommands.set(shortLink, link);
        syncCommands.expire(shortLink, dayInSeconds * 90);
        syncCommands.select(DB_STATISTICS_NUMBER);
        syncCommands.zadd("visits", 0, shortLink);
        String domainName = Util.getDomainName(link);
        if (!domainName.equals("")
                && (syncCommands.zscore("visits_by_domain", domainName) == null
                || syncCommands.zscore("visits_by_domain", domainName).equals
                (0))) {
            syncCommands.zadd("visits_by_domain", 0, domainName);
        }

        connection.close();
        return shortLink;
    }

    @Override
    public String get(String shortLink) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_LINK_NUMBER);
        String link = syncCommands.get(shortLink);
        syncCommands.select(DB_STATISTICS_NUMBER);
        syncCommands.zincrby("visits", 1, shortLink);
        syncCommands.zincrby("visits_by_domain", 1, Util.getDomainName(link));

        connection.close();
        return link;
    }
}
