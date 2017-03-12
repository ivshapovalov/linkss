package ru.ivan.linkss.repository;


import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.junit.*;
import ru.ivan.linkss.repository.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Ignore
public class RepositoryTest {

//    private static final RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));
//    private static final RedisClient redisClientLinks = RedisClient.create(System.getenv("HEROKU_REDIS_AMBER_URL"));
//    private static final LinkRepository repository = new RedisTwoDBLinkRepositoryImpl(redisClient, redisClientLinks);
//
//    //REDIS 1
//    private static final int DB_WORK_NUMBER = 0;
//    private static final int DB_FREELINK_NUMBER = 1;
//    //REDIS 2
//    private static final int DB_LINK_NUMBER = 0;
//    private static final long MIN_FREE_LINK_SIZE = 10;
//
//    private static final String KEY_USERS = "_users";
//    private static final String KEY_LINKS = "_links";
//    private static final String KEY_VISITS = "_visits";
//    private static final String KEY_VISITS_BY_DOMAIN = "_visits_by_domain";
//    private static final String KEY_PREFERENCES = "_preferences";
//    private static final String KEY_LENGTH = "key.length";
//
//    private static final String DEFAULT_USER = "user";
//    private static final String DEFAULT_PASSWORD = "user";
//    private static final String ADMIN_USER = "admin";
//    private static final String ADMIN_PASSWORD = "admin";
//
//    private StatefulRedisConnection<String, String> connection;
//    private RedisCommands<String, String> syncCommands;
//    private StatefulRedisConnection<String, String> connectionLinks;
//    private RedisCommands<String, String> syncCommandsLinks;

//    @BeforeClass
//    public static void init() {
//
//    }
//
//    @Before
//    public void before() {
//        repository.init();
//        connection = connect();
//        syncCommands = connection.sync();
//        connectionLinks = connectLinks();
//        syncCommandsLinks = connectionLinks.sync();
//        syncCommands.select(DB_FREELINK_NUMBER);
//        //syncCommands.flushdb();
//
//    }
//
//    @After
//    public void after() {
//        syncCommands.flushall();
//        connection.close();
//        syncCommandsLinks.flushall();
//        connectionLinks.close();
//    }
//
//    private StatefulRedisConnection<String, String> connect() {
//        boolean connectionFailed = true;
//        StatefulRedisConnection<String, String> connection = null;
//        do {
//            try {
//                connection = redisClient.connect();
//                connectionFailed = false;
//            } catch (Exception e) {
//                connectionFailed = true;
//            }
//        } while (connectionFailed);
//        return connection;
//    }
//
//    private StatefulRedisConnection<String, String> connectLinks() {
//        boolean connectionFailed = true;
//        StatefulRedisConnection<String, String> connection = null;
//        do {
//            try {
//                connection = redisClientLinks.connect();
//                connectionFailed = false;
//            } catch (Exception e) {
//                connectionFailed = true;
//            }
//        } while (connectionFailed);
//        return connection;
//    }
//
//
//    @Test
//    public void initTest() {
//        syncCommands.select(DB_WORK_NUMBER);
//        String expected = "255";
//        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, expected);
//        String actual = syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void getDBLinkSizeTest() {
//        Long expected = 0L;
//        Long actual = repository.getDBLinksSize();
//        assertEquals(expected, actual);
//
//        //
//        repository.createKeys(1);
//
//        repository.createShortLink(new User.UserBuilder()
//                .addUserName("user")
//                .addPassword("password")
//                .build(), "ya.ru");
//        repository.createShortLink(new User.UserBuilder()
//                .addUserName("user")
//                .addPassword("password")
//                .build(), "mail.ru");
//        expected = 2L;
//        actual = repository.getDBLinksSize();
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void getDBFreeLinkSizeTest() {
//        repository.createKeys(1);
//        Long expected = 62L;
//        Long actual = repository.getDBFreeLinksSize();
//        assertEquals(expected, actual);
//
//    }
//
//    @Test
//    public void getRandomShortLinkTest() {
//        syncCommandsLinks.select(DB_LINK_NUMBER);
//        syncCommandsLinks.set("shortLink", "link");
//        String expected = "shortLink";
//        String actual = repository.getRandomShortLink();
//        assertEquals(expected, actual);
//
//    }
//
//    @Test
//    public void createShortLinkTest() {
//        syncCommands.select(DB_FREELINK_NUMBER);
//        syncCommands.append("shortLink", "");
//        String expected = "shortLink";
//        String expectedLink = "link";
//        String actual = repository.createShortLink(
//                new User.UserBuilder()
//                .addUserName("user")
//                .addPassword("password")
//                .build(), expectedLink);
//        String actualLink = repository.getLink(actual);
//        assertEquals(expected, actual);
//        assertEquals(expectedLink, actualLink);
//
//    }
//
//    @Test
//    public void createUserTest() {
//        syncCommands.select(DB_WORK_NUMBER);
//        String userName = "user1";
//        String expectedPassword = "password";
//        repository.createUser(new User.UserBuilder()
//                .addUserName(userName)
//                .addPassword(expectedPassword)
//                .build());
//        String actualPassword = syncCommands.hget(KEY_USERS, userName);
////        assertEquals(expectedPassword,actualPassword);
//        assertThat(expectedPassword, is(actualPassword));
//
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void createExistedUserTest() {
//        syncCommands.select(DB_WORK_NUMBER);
//        String userName = "user";
//        String expectedPassword = "password";
//
//        repository.createUser(new User.UserBuilder()
//                .addUserName(userName)
//                .addPassword(expectedPassword)
//                .build());
//
//    }
//
//    @Test
//    @Ignore
//    public void testScan()
//
//    {
//        List<String> freeLinks = new ArrayList<>();
//        syncCommands.select(DB_FREELINK_NUMBER);
//        KeyScanCursor cursor = syncCommands.scan();
//        while (!cursor.isFinished()) {
//            List<String> keys = cursor.getKeys();
//            keys.stream().forEach(key -> freeLinks.add(key));
//            cursor = syncCommands.scan(cursor);
//        }
//    }
//
//    @Test
//    @Ignore
//    public void testUrl()
//
//    {
//        String link="  https://dashboard.heroku.com/apps/linkss/deploy/github";
//        match(link);
//
//    }
//
//    void match(String link) {
//        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\" +
//                ".[a-z0-9-]+)+([/?].*)?$";
//        Pattern p = Pattern.compile(URL_REGEX);
//        Matcher m = p.matcher(link);
//        System.out.println(m.find()+":"+link);
//
//
//    }

}
