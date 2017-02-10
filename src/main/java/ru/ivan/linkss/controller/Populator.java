package ru.ivan.linkss.controller;


import com.lambdaworks.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.service.LinksServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

@Component
public class Populator {
    private static final int SIZE_OF_POOL = 5;
    private static final int USERS = 100;
    private static final int W_REQUESTS = 500;
    private static final int R_REQUESTS = 10000;
    private static final int RW_REQUESTS = 5000;

    @Autowired
    private LinksService service;

    @Autowired
    @Qualifier(value = "repositoryTwo")
    private LinkRepository repository;


    private String path;
    private String context;

    final static List<String> domains = new ArrayList<>();

    List<User> users;

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
//                RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
//       RedisClient redisClientLinks = RedisClient.create
//            ("redis://h:p3c1e48009e2ca7405945e112b198385d800c695c79095312007c06ab48285e70@ec2-54-163-250-167.compute-1.amazonaws.com:18529");
        RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));
        RedisClient redisClientLinks = RedisClient.create(System.getenv
                ("HEROKU_REDIS_AMBER_URL"));
        ;

        RedisTwoDBLinkRepositoryImpl repository = new RedisTwoDBLinkRepositoryImpl(redisClient,
                redisClientLinks);
        Populator populator = new Populator();

        LinksServiceImpl service = new LinksServiceImpl();
        service.setRepository(repository);
        populator.setRepository(repository);
        populator.setService(service);
        populator.setContext("localhost:8080\\");
        populator.setContext("links.herokuapp.com\\");
        populator.setPath("C:\\JavaStudy\\my\\linkss\\target\\linkss\\");
        populator.init();

    }

    public void init() {

        repository.init();

        users = new ArrayList<>();
        for (int i = 0; i < USERS; i++) {
            try {
                User user = new User("user" + i);
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
        Random random=new Random();
        for (int i = 1; i <= RW_REQUESTS; i++) {
            Runnable client;
            User user=users.get(random.nextInt(users.size()-1));
            if (isWrite) {
                client = new Creator(user,i);
            } else {
                client = new Visitor(user,i);
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
        Random random=new Random();
        for (int i = 1; i <= R_REQUESTS; i++) {
            User user=users.get(random.nextInt(users.size()-1));
            Runnable reader = new Visitor(user,i);
            executor.execute(reader);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(SIZE_OF_POOL);
        Random random=new Random();
        for (int i = 1; i <= W_REQUESTS; i++) {

            User user= null;
            int a=random.nextInt(users.size()-1);
            try {
                user = users.get(a);
            } catch (Exception e) {
                System.out.println(a);
            }
            Runnable creator = new Creator(user,i);
            executor.execute(creator);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateInOneThread() {
        Random random=new Random(users.size());
        for (int i = 1; i <= W_REQUESTS; i++) {
            User user=users.get(random.nextInt(users.size()-1));
            new Creator(user,i).run();
        }

    }

    private void executeReadInOneThread() {
        Random random=new Random();
        for (int i = 1; i <= R_REQUESTS; i++) {
            User user=users.get(random.nextInt(users.size()-1));
            new Visitor(user,i).run();
        }
    }

    private class Creator extends Client {

        public Creator(User user,int number) {
            super(user,number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            String link = getRandomDomain() + "/" + number;
            String shortLink = service.createShortLink(user,
                    link, path,
                    context);
            //            service.uploadImage();


            if (shortLink == null) {
                System.out.println(Thread.currentThread().getName() +
                        ": free short links ended ");
//            } else {
//                    System.out.println(Thread.currentThread().getName() +
//                           ": create '" + shortLink + "' - link '" + link + "'");
            }
        }
    }

    private class Visitor extends Client {

        public Visitor(User user,int number) {
            super(user,number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            boolean failed = true;
            do {
                String shortLink = service.getRandomShortLink();
                if (!"".equals(shortLink)) {
                    String link = null;
                    link = service.visitLink(shortLink);
//                            System.out.println(Thread.currentThread().getName() +
//                                    ": get '" + shortLink + "': '" + link + "'");
                    failed = false;
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

    }
}
