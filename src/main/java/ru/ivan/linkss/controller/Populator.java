package ru.ivan.linkss.controller;


import com.lambdaworks.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisOneDBLinkRepositoryImpl;
import ru.ivan.linkss.repository.RepositoryException;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.service.LinksServiceImpl;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

@Component
public class Populator {
    private static final int SIZE_OF_POOL = 5;
    private static final int USERS = 6;
    private static final int W_REQUESTS = 100;
    private static final int R_REQUESTS = 1000;
    private static final int RW_REQUESTS = 1000;

    @Autowired
    private LinksService service;

    @Autowired
    @Qualifier(value = "repositoryOne")
    private LinkRepository repository;


    private String path;
    private String context;

    final static List<String> domains = new ArrayList<>();

    static {
        domains.add("ya.com");
        domains.add("yandex.com");
        domains.add("yahoo.com");
        domains.add("google.ua");
        domains.add("mail.us");
        domains.add("google.au");
        domains.add("google.mu");
        domains.add("google.yu");
        domains.add("google.qu");
        domains.add("google.iu");
        domains.add("gmail.au");
        domains.add("news.com");
        domains.add("nyce.fr");
        domains.add("mail.sp");
        domains.add("mail.gf");
        domains.add("mail.de");
        domains.add("mail.re");
        domains.add("mail.po");

    }

    final static List<String> userAgents = new ArrayList<>();

    static {
        userAgents.add("Mozilla/5.0 (X11; U; Linux; i686; en-US; rv:1.6) Gecko Debian/1.6-7");
        userAgents.add("Konqueror/3.0-rc4; (Konqueror/3.0-rc4; i686 Linux;;datecode)");
        userAgents.add("MSIE (MSIE 6.0; X11; Linux; i686) Opera 7.23");
        userAgents.add("Lynx/2.8.5rel.1 libwww-FM/2.14 SSL-MM/1.4.1 GNUTLS/0.8.12");
        userAgents.add("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        userAgents.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
        userAgents.add("Mozilla/5.0 (Windows; U; Win98; en-US; rv:1.4) Gecko Netscape/7.1 (ax)");
        userAgents.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.29 Safari/525.13");
        userAgents.add("Avant Browser/1.2.789rel1 (http://www.avantbrowser.com)");
        userAgents.add("Opera/9.25 (Windows NT 6.0; U; en)");
        userAgents.add("Mozilla/2.02E (Win95; U)");


    }

    List<User> users;

    public Populator() {
    }

    public void setService(LinksService service) {
        this.service = service;
    }

    public void setRepository(LinkRepository repository) {
        this.repository = repository;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create
                (System.getenv
                        ("REDIS_URL"));

        RedisOneDBLinkRepositoryImpl repository = new RedisOneDBLinkRepositoryImpl(redisClient);
        Populator populator = new Populator();

        LinksServiceImpl service = new LinksServiceImpl();
        service.setRepository(repository);
        String path = "C:\\JavaStudy\\my\\linkss\\target\\linkss\\";
        service.clear(path);
        populator.setRepository(repository);
        populator.setService(service);
        populator.setContext("localhost:8080\\links\\");
        populator.setPath(path);
        populator.init();

    }

    public void init() {

        users = new ArrayList<>();
        for (int i = 0; i < USERS; i++) {
            try {
                User user = new User.Builder().addUserName("user" + i).build();
                service.createUser(user);
                users.add(user);
            } catch (Exception e) {
            }

        }

        long startTime = System.nanoTime();
        //executeCreateInOneThread();
        executeCreateMultiThread();
        long linksSize = service.getDBLinksSize();
        long freeLinksSize = service.getDBFreeLinksSize();
        long endTime = System.nanoTime();
        System.out.println(String.valueOf(W_REQUESTS) + " write: " + (endTime -
                startTime) / 1000 + " millis");
        System.out.println(String.format("links: %s, free: %s", linksSize, freeLinksSize));

        startTime = System.nanoTime();
        //executeReadInOneThread();

        executeReadMultiThread();
        endTime = System.nanoTime();
        System.out.println(String.valueOf(R_REQUESTS) + " read: " + (endTime -
                startTime) / 1000 + " millis");


        startTime = System.nanoTime();
        //executeCreateReadMultiThread();
        endTime = System.nanoTime();
        System.out.println(String.valueOf(RW_REQUESTS) + " read/write: " + (endTime -
                startTime) / 1000 + " millis");

    }

    private void executeCreateReadMultiThread() {
        boolean isWrite = true;
        ExecutorService executor = Executors.newFixedThreadPool(SIZE_OF_POOL);
        Random random = new Random();
        for (int i = 1; i <= RW_REQUESTS; i++) {
            Runnable client;
            User user = users.get(random.nextInt(users.size() - 1));
            if (isWrite) {
                client = new Creator(user, i);
            } else {
                client = new Visitor(user, i);
            }
            executor.execute(client);
            isWrite = !isWrite;
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeReadMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(SIZE_OF_POOL);
        Random random = new Random();
        for (int i = 1; i <= R_REQUESTS; i++) {
            User user = users.get(random.nextInt(users.size() - 1));
            Runnable reader = new Visitor(user, i);
            executor.execute(reader);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(SIZE_OF_POOL);
        Random random = new Random();
        for (int i = 1; i <= W_REQUESTS; i++) {

            User user = null;
            int a = random.nextInt(users.size() - 1);
            try {
                user = users.get(a);
            } catch (Exception e) {
                System.out.println(a);
            }
            Runnable creator = new Creator(user, i);
            executor.execute(creator);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateInOneThread() {
        Random random = new Random(users.size());
        for (int i = 1; i <= W_REQUESTS; i++) {

            User user = users.get(random.nextInt(users.size() - 1));
            new Creator(user, i).run();
        }

    }

    private void executeReadInOneThread() {
        Random random = new Random();
        for (int i = 1; i <= R_REQUESTS; i++) {
            User user = users.get(random.nextInt(users.size() - 1));
            new Visitor(user, i).run();
        }
    }

    private class Creator extends Client {
        public Creator(User user, int number) {
            super(user, number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            Map<String, String> params = new HashMap<>();
            params.put("ip", getRandomIp());
            params.put("user-agent", getRandomUserAgent());

            String link = getRandomDomain() + "/" + number;
            String shortLink = null;
            try {
                shortLink = service.createShortLink(user,
                        link, path,
                        context,params);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }

            if (shortLink == null) {
                System.out.println(Thread.currentThread().getName() +
                        ": free short links ended ");
            }
        }
    }

    private class Visitor extends Client {

        public Visitor(User user, int number) {
            super(user, number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            boolean failed = true;
            do {
                String shortLink = service.getRandomShortLink();
                if (!"".equals(shortLink)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("ip", getRandomIp());
                    params.put("user-agent", getRandomUserAgent());
                    String link = service.visitLinkwithIpChecking(shortLink, params);
                    if (link == null) {
                        failed = true;
                    } else {
                        failed = false;
                    }
                } else {
                    failed = true;
                    System.out.println("random link null. retry");
                }
            } while (failed);

        }


    }

    private abstract class Client implements Runnable {
        protected final User user;
        protected final int number;
        protected final Random random = new Random();

        public Client(User user, int number) {
            this.user = user;
            this.number = number;
        }

        @Override
        public void run() {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected String getRandomDomain() {
            int index = Math.abs(random.nextInt() % domains.size());
            return domains.get(index);
        }

        protected String getRandomUserAgent() {
            int index = Math.abs(random.nextInt() % userAgents.size());
            return userAgents.get(index);
        }

        protected String getRandomIp() {
            StringBuilder ip = new StringBuilder();

            for (int i = 1; i <= 4; i++) {
                int num = (int) (Math.random() * 255);
                ip.append(num).append(".");
            }
            return ip.toString().substring(0, ip.length() - 1);
        }

    }
}
