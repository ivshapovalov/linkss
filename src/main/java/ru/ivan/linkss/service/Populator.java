package ru.ivan.linkss.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

@Component
public class Populator {
    private static final int sizeOfPool = 15;
    private static final int requests = 1000;

    @Autowired
    private LinksService service;

    @Autowired
    @Qualifier(value = "repositoryTwo")
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

    public Populator() {
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Populator(String path, String context) {
        this.path = path;
        this.context = context;
    }

    public static void main(String[] args) {
        new Populator("","").init();
    }
    public void init() {

//        RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
//       RedisClient redisClientLinks = RedisClient.create
//            ("redis://h:p3c1e48009e2ca7405945e112b198385d800c695c79095312007c06ab48285e70@ec2-54-163-250-167.compute-1.amazonaws.com:18529");
//        //RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));

//        RedisTwoDBLinkRepositoryImpl repository=new RedisTwoDBLinkRepositoryImpl(redisClient,
//                redisClientLinks);
        repository.init();

        long startTime = System.nanoTime();
        //executeCreateInOneThread();
        executeCreateMultiThread();
        long linksSize=service.getDBLinksSize();
        long freeLinksSize=service.getDBFreeLinksSize();
        long endTime = System.nanoTime();
        System.out.println(String.valueOf(requests) + " write: " + (endTime -
                startTime) / 1000 + " millis");
        System.out.println(String.format("links: %s, free: %s",linksSize,freeLinksSize));

        startTime = System.nanoTime();
        //executeReadInOneThread();

        executeReadMultiThread();
        endTime = System.nanoTime();
        System.out.println(String.valueOf(requests) + " read: " + (endTime -
                startTime) / 1000 + " millis");


        startTime = System.nanoTime();
        executeCreateReadMultiThread();
        endTime = System.nanoTime();
        System.out.println(String.valueOf(requests) + " read/write: " + (endTime -
                startTime) / 1000 + " millis");

    }

    private void executeCreateReadMultiThread() {
        boolean isWrite=true;
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable client;
            if (isWrite) {
                client = new Creator(i);
            } else {
                client = new Visitor(i);
            }
            executor.execute(client);
            isWrite = !isWrite;
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeReadMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable reader = new Visitor(i);
            executor.execute(reader);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable creator = new Creator(i);
            executor.execute(creator);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private void executeCreateInOneThread() {
        for (int i = 1; i <= requests; i++) {
            new Creator(i).run();
        }

    }

    private void executeReadInOneThread() {

        for (int i = 1; i <= requests; i++) {
            new Visitor(i).run();
        }
    }

    private class Creator extends Client {

        public Creator(int number) {
            super(number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            String link = getRandomDomain() + "/" + number;
            String shortLink = service.createShortLink(new User("user","user"), link);
 //            service.uploadImage();
            String imagePath=path+"resources//" +shortLink + ".png";
            service.uploadImage(imagePath, shortLink, context + shortLink);


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

        public Visitor(int number) {
            super(number);
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
                    failed=false;
                } else {
                    failed = true;
                    System.out.println("random link null. retry");
                }
            } while (failed);

        }
    }

    private abstract class Client implements Runnable {
        protected final int number;
        protected final Random random = new Random();

        public Client(int number) {
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
