package ru.ivan.linkss.repository;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.junit.*;

import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl;
import ru.ivan.linkss.repository.entity.User;

import static org.junit.Assert.assertEquals;

@Ignore
public class RepositoryTest {

    private static final LinkRepository repository=new RedisTwoDBLinkRepositoryImpl();
    private static final RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));

    private static final int DB_WORK_NUMBER = 0;
    private static final int DB_FREELINK_NUMBER = 1;
    private static final long MIN_FREE_LINK_SIZE = 10;

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

    private StatefulRedisConnection<String, String> connection;
    RedisCommands<String, String> syncCommands;

    @BeforeClass
    public static void init() {

    }

    @Before
    public void before(){
        repository.init();
        connection = connect();
        syncCommands = connection.sync();

    }
    @After
    public void after(){
        syncCommands.flushall();
        connection.close();
    }

    private static StatefulRedisConnection<String, String> connect() {
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

    @Test
    public void initTest()
    {
        syncCommands.select(DB_WORK_NUMBER);
        String expected="255";
        syncCommands.hset(KEY_PREFERENCES, KEY_LENGTH, expected);
        String actual=syncCommands.hget(KEY_PREFERENCES, KEY_LENGTH);
        assertEquals(expected, actual);
    }

    @Test
    public void getDBLinkSizeTest()
    {
        syncCommands.select(DB_WORK_NUMBER);
        Long expected=0L;
        Long actual=syncCommands.hlen(KEY_LINKS);
        assertEquals(expected,actual);

        //
        syncCommands.hset(KEY_LINKS,"aaa","value1");
        syncCommands.hset(KEY_LINKS,"bbb","value2");
        expected=2L;
        actual=repository.getDBLinksSize();
        assertEquals(expected,actual);
    }

    @Test
    public void getDBFreeLinkSizeTest()
    {
        syncCommands.select(DB_FREELINK_NUMBER);
        repository.createKeys(1);
        Long expected=62L;
        Long actual=repository.getDBFreeLinksSize();
        assertEquals(expected,actual);

    }

    @Test
    public void getRandomShortLinkTest()
    {
        syncCommands.select(DB_WORK_NUMBER);
        syncCommands.hset(KEY_LINKS,"shortLink","link");
        String expected="shortLink";
        String actual=repository.getRandomShortLink();
        assertEquals(expected,actual);

    }

    @Test
    public void createShortLinkTest()
    {
        syncCommands.select(DB_FREELINK_NUMBER);
        syncCommands.append("shortLink","");
        String expected="shortLink";
        String expectedLink="link";
        String actual=repository.createShortLink(new User(),expectedLink);
        String actualLink=repository.getLink(actual);
        assertEquals(expected,actual);
        assertEquals(expectedLink,actualLink);

    }
    @Test
    public void createUserTest()
    {
        syncCommands.select(DB_WORK_NUMBER);
        String user="user1";
        String expectedPassword="password";
        repository.createUser(user,expectedPassword);
        String actualPassword=syncCommands.hget(KEY_USERS,user);
        assertEquals(expectedPassword,actualPassword);

    }

    @Test (expected = RuntimeException.class)
    public void createExistedUserTest()
    {
        syncCommands.select(DB_WORK_NUMBER);
        String user="user";
        String expectedPassword="password";
        repository.createUser(user,expectedPassword);

    }



}
