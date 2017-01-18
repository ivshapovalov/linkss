package ru.ivan.linkss;


import ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl;
import ru.ivan.linkss.service.LinkssServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;


public class TestConnection {
    private static int sizeOfPool = 5;
    private static int requests = 1000;

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


        service = new LinkssServiceImpl();
        service.setRepository(new RedisTwoDBLinkRepositoryImpl());
        //String shortLink = service.getRandomShortLink();
        //System.out.println(shortLink);

        ///WRITE

        long startTime = System.nanoTime();

        executeCreateInOneThread();
        //ExecutorService executor = Executors.newFixedThreadPool(sizeOfPool);
//        for (int i = 1; i <= requests; i++) {
//            Runnable creator = new Thread(new Creator(i), "t" + i);
//            executor.execute(creator);
//        }
//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }
        long endTime = System.nanoTime();
//
        System.out.println(String.valueOf(requests)+" write: "+(endTime -
                startTime) / 1000000000+" seconds");

        //READ

        startTime = System.nanoTime();
//        executor = Executors.newFixedThreadPool(sizeOfPool);
//        for (int i = 1; i <= requests; i++) {
//            Runnable reader = new Thread(new Reader(i), "t" + i);
//            executor.execute(reader);
//        }
//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }

        executeReadInOneThread();

        endTime = System.nanoTime();

        System.out.println(String.valueOf(requests) + " read: " + (endTime -
                startTime) / 1000000000 + " seconds");

//        //READ & WRITE
//        Random random = new Random();
//        boolean isWrite = false;
//        startTime = System.nanoTime();
//        executor = Executors.newFixedThreadPool(sizeOfPool);
//        for (int i = 1; i <= requests; i++) {
//            isWrite = false;
//
//            if (random.nextInt(10000) % 2 == 0) {
//                isWrite = true;
//            }
//            Runnable client;
//            if (isWrite) {
//                client = new Thread(new Creator(i), "t" + i);
//            } else {
//                client = new Thread(new Reader(i), "t" + i);
//            }
//            executor.execute(client);
//        }
//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }
        endTime = System.nanoTime();

        System.out.println(String.valueOf(requests) + " read/write: " + (endTime -
                startTime) / 1000000000 + " seconds");
    }

    private static void executeCreateInOneThread() {
        for (int i = 1; i <= requests; i++) {
            new Creator(i).run();
        }

    }
    private static void executeReadInOneThread() {

        for (int i = 1; i <= requests; i++) {
            new Reader(i).run();
        }
    }

    private static class Creator extends Client {

        public Creator(int number) {
            super(number);
        }

        @Override
        public void run() {
            super.run();
            String link = getRandomDomain() + "/" + number;
            String shortLink = service.createShortLink("user", link);
            if (shortLink == null) {
//                    System.out.println(Thread.currentThread().getName() +
//                            ": free short links ended ");
            } else {
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
            super.run();
            boolean failed = false;
            do {
                String shortLink = service.getRandomShortLink();
                if (shortLink != null && !"".equals(shortLink)) {
                    String link = null;
                    try {
                        link = service.getLink(shortLink);
//                        System.out.println(Thread.currentThread().getName() +
//                                ": get s '" + shortLink + "': l '" + link + "'");
                    } catch
                            (Exception e) {
                        System.out.println(shortLink);

                    }

                } else {
                    failed = true;
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
