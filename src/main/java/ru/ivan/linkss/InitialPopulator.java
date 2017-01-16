package ru.ivan.linkss;


import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;
import ru.ivan.linkss.service.KeyCreator;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

public class InitialPopulator {

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
//                new KeyCreator().createShortLink(3);
//            }
//        },"t3").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().createShortLink(2);
//            }
//        },"t2").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().createShortLink(6);
//            }
//        },"t6").start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new KeyCreator().createShortLink(5);
//            }
//        },"t5").start();

        BigInteger keyCount=new KeyCreator().create(2);
        System.out.println(String.format("'%s' ключей добавлено",keyCount.toString()));


    }


}
