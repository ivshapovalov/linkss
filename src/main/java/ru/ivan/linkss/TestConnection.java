package ru.ivan.linkss;


import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;
import ru.ivan.linkss.service.LinkssServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;


public class TestConnection {
    private static int sizeOfPool = 50;
    private static int requests = 1000000;

    private static LinkssServiceImpl service;

    final List<String> domains = new ArrayList<>();

    {
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

        service = new LinkssServiceImpl();
        service.setRepository(new RedisLinkRepositoryImpl());
        //String shortLink = service.getRandomShortLink();
        //System.out.println(shortLink);
        ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
        for (int i = 1; i <= requests; i++) {
            Runnable worker = new Thread(new TestConnection().new Client(i), "t" + i);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");

    }

    class Client implements Runnable {
        final Random random = new Random();

        private int number;

        public Client(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            // read or write
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean isWrite = false;
            if (random.nextInt(10000) % 2 == 0) {
                isWrite = true;
            }
            if (isWrite) {
                String link = getRandomDomain() + "/" + number;
                String shortLink = service.createShortLink("user", link);
                if (shortLink == null) {
//                    System.out.println(Thread.currentThread().getName() +
//                            ": free short links ended ");
                } else {
//                    System.out.println(Thread.currentThread().getName() +
//                            ": createShortLink '" + shortLink + "' - link '" + link + "'");
                }
            } else {
                String shortLink = service.getRandomShortLink();
                if (!"".equals(shortLink)) {
                    String link = service.getLink(shortLink);
//                    System.out.println(Thread.currentThread().getName() +
//                            ": getLink '" + shortLink + "' - link '" + link + "'");
                }
            }
        }

        private String getRandomDomain() {
            int index = Math.abs(random.nextInt() % 10);
            return domains.get(index);
        }

    }
}
