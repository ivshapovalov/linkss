package ru.ivan.linkss.repository;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RedisLinkRepositoryImpl implements LinkRepository {

    private static final int DB_LINK_NUMBER = 0;
    private static final int DB_PREFERENCES_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;
    private static final int DB_STATISTICS_NUMBER = 1;
    private static final String EXPIRE_PERIOD= "expire.period";
    private static final String KEY_LENGTH= "key.length";

  //private RedisClient redisClient = RedisClient.create("redis://localhost:6379/0");
//    private RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
//    private RedisClient redisClientStat = RedisClient.create
//            ("redis://h:p7c4dd823e40671d79ccbc943c29c2f0aec03d38cc29627b165cb1f32985fd766@ec2-54-221-228-237.compute-1.amazonaws.com:18689");
    private RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));
    private RedisClient redisClientStat = RedisClient.create(System.getenv
            ("HEROKU_REDIS_COBALT_URL"));
    private long dayInSeconds = 60 * 60 * 24;

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
        StatefulRedisConnection<String, String> connectionStat = redisClientStat.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        RedisCommands<String, String> syncCommandsStat = connectionStat.sync();

        //syncCommands.configSet("databases","16");
        syncCommands.flushall();
        syncCommandsStat.flushall();
        syncCommandsStat.select(DB_PREFERENCES_NUMBER);
        syncCommandsStat.set(KEY_LENGTH, String.valueOf("4"));
        syncCommandsStat.set(EXPIRE_PERIOD, "30");
        //syncCommandsStat.bgsave();
        connectionStat.close();
        //redisClient.shutdown();
    }

    @Override
    public List<List<String>> getShortStat() {
        StatefulRedisConnection<String, String> connectionStat = redisClientStat.connect();
        RedisCommands<String, String> syncCommandsStat = connectionStat.sync();
        syncCommandsStat.select(DB_STATISTICS_NUMBER);
        List<ScoredValue<String>> list=syncCommandsStat.zscan("visits_by_domain")
                .getValues();
        List<List<String>> shortStat = list.stream()
                .map(p -> {
                    List<String> l = new ArrayList<>();
                    l.add(p.getValue());
                    l.add(String.valueOf((int)p.getScore()));
                    return l;
                }).collect(Collectors.toList())
                ;
        connectionStat.close();

        return shortStat;
    }

    @Override
    public List<FullLink> getFullStat(String contextPath) {
        StatefulRedisConnection<String, String> connectionStat = redisClientStat.connect();
        RedisCommands<String, String> syncCommandsStat = connectionStat.sync();
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        syncCommands.select(DB_LINK_NUMBER);
        syncCommandsStat.select(DB_STATISTICS_NUMBER);
        List<ScoredValue<String>> list=syncCommandsStat.zscan("visits")
                .getValues();
        List<FullLink> fullStat = list.stream()
                .map(p -> {
                    String shortLink=contextPath+p.getValue();
                    String link=syncCommands.get(p.getValue());
                    return new FullLink(shortLink,
                            link,String.valueOf((int)p.getScore()),
                            shortLink+".png");
                }).collect(Collectors.toList())
                ;
        connectionStat.close();

        return fullStat;
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
        StatefulRedisConnection<String, String> connectionStat = redisClientStat.connect();
        RedisCommands<String, String> syncCommandsStat = connectionStat.sync();
        syncCommandsStat.select(DB_PREFERENCES_NUMBER);
        String days=syncCommandsStat.get(EXPIRE_PERIOD);
        syncCommands.select(DB_FREELINK_NUMBER);
        String shortLink = syncCommands.randomkey();
        syncCommands.del(shortLink);
        syncCommands.select(DB_LINK_NUMBER);
        syncCommands.set(shortLink, link);
        syncCommands.expire(shortLink, dayInSeconds * Integer.valueOf(days));
        syncCommandsStat.select(DB_STATISTICS_NUMBER);
        syncCommandsStat.zadd("visits", 0, shortLink);
        String domainName = Util.getDomainName(link);
        if (!domainName.equals("")
                && (syncCommandsStat.zscore("visits_by_domain", domainName) == null
                || syncCommandsStat.zscore("visits_by_domain", domainName)==0
                )) {
            syncCommandsStat.zadd("visits_by_domain", 0, domainName);
        }

        connection.close();
        connectionStat.close();
        return shortLink;
    }

    @Override
    public String get(String shortLink) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        StatefulRedisConnection<String, String> connectionStat = redisClientStat.connect();
        RedisCommands<String, String> syncCommandsStat = connectionStat.sync();
        syncCommands.select(DB_LINK_NUMBER);
        String link = syncCommands.get(shortLink);
        syncCommandsStat.select(DB_STATISTICS_NUMBER);
        syncCommandsStat.zincrby("visits", 1, shortLink);
        syncCommandsStat.zincrby("visits_by_domain", 1, Util.getDomainName(link));
        connection.close();
        connectionStat.close();
        return link;
    }
}
