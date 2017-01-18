package ru.ivan.linkss;


import ru.ivan.linkss.repository.RedisOneDBLinkRepositoryImpl;
import ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl;

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

        RedisOneDBLinkRepositoryImpl redisOneDBLinkRepository = new RedisOneDBLinkRepositoryImpl();
        redisOneDBLinkRepository.init();
        BigInteger keyCount=redisOneDBLinkRepository.new KeyCreator().create(1);
        System.out.println(String.format("'%s' ключей добавлено",keyCount.toString()));


    }


}
