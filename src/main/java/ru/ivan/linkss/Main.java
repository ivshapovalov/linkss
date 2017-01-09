package ru.ivan.linkss;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;
import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) {
        init();
//        String domain=getDomainName("http://ya.ru/sdfasfas");
//        System.out.println(domain);
    }

    public static String getDomainName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static void init() {

        new RedisLinkRepositoryImpl().init();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().create(3);
//            }
//        },"t3").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().create(2);
//            }
//        },"t2").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().create(6);
//            }
//        },"t6").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().create(5);
//            }
//        },"t5").start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new KeyCreator().create(3);
            }
        },"t4").start();

    }


}
