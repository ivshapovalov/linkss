package ru.ivan.linkss;


import com.lambdaworks.redis.RedisClient;
import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisLocalDBLinkRepositoryImpl;
import ru.ivan.linkss.repository.RedisOneDBLinkRepositoryImpl;
import ru.ivan.linkss.service.LinkssServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;


public class TestConnection {
    private static int sizeOfPool = 5;
    private static int requests = 10000;

    private static LinkssServiceImpl service;

    final static List<String> domains = new ArrayList<>();

    static {
        domains.add("ya.com");
        domains.add("yandex.com");
        domains.add("yahoo.com");
        domains.add("google.ua");
        domains.add("mail.us");
        domains.add("google.au");
        domains.add("gmail.au");
        domains.add("news.com");
        domains.add("nyce.fr");
        domains.add("mail.sp");

    }

    public static void main(String[] args) {

//        RedisClient redisClient = RedisClient.create
//            ("redis://h:p719d91a83883803e0b8dcdd866ccfcd88cb7c82d5d721fcfcd5068d40c253414@ec2-107-22-239-248.compute-1.amazonaws.com:14349");
        //RedisClient redisClient = RedisClient.create(System.getenv("REDIS_URL"));

        RedisOneDBLinkRepositoryImpl repository=new RedisOneDBLinkRepositoryImpl();
        repository.init();
        service = new LinkssServiceImpl();
        service.setRepository(repository);

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
        //executeCreateInOneThread();
        executeCreateMultiThread();
        endTime = System.nanoTime();
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

    private static void executeCreateReadMultiThread() {
        Random random = new Random();
        boolean isWrite = false;
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        isWrite = true;
        for (int i = 1; i <= requests; i++) {

//            if (random.nextInt(10000) % 2 == 0) {
//                isWrite = true;
//            }
            Runnable client;
            if (isWrite) {
                client = new Creator(i);
            } else {
                client = new Reader(i);
            }
            executor.execute(client);
            isWrite = !isWrite;
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private static void executeReadMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable reader = new Reader(i);
            executor.execute(reader);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private static void executeCreateMultiThread() {
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable creator = new Creator(i);
            executor.execute(creator);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private static void executeCreateInOneThread() {
        for (int i = 1; i <= requests; i++) {
            new Creator(i).run();
        }

    }

    private static void executeReadInOneThread() {

        for (int i = 1; i <= requests; i++) {
            new Reader(i).run();
            System.out.println(i);
        }
    }

    private static class Creator extends Client {

        public Creator(int number) {
            super(number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            String link = getRandomDomain() + "/" + number;
            String shortLink = service.createShortLink("user", link);
            if (shortLink == null) {
                    System.out.println(Thread.currentThread().getName() +
                            ": free short links ended ");
//            } else {
//                    System.out.println(Thread.currentThread().getName() +
//                           ": create '" + shortLink + "' - link '" + link + "'");
            }
        }
    }

    private static class Reader extends Client {

        public Reader(int number) {
            super(number);
        }

        @Override
        public void run() {
            Thread.currentThread().setName("t" + number);

            boolean failed = true;
            do {
                String shortLink = service.getRandomShortLink();
                if (shortLink != null && !"".equals(shortLink)) {
                    String link = null;
                    link = service.getLink(shortLink);
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

    private static abstract class Client implements Runnable {
        protected int number;
        protected Random random = new Random();

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
            int index = Math.abs(random.nextInt() % 10);
            return domains.get(index);
        }

    }
}
