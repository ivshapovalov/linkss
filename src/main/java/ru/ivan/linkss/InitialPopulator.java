package ru.ivan.linkss;


import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisTwoDBLinkRepositoryImpl;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

public class InitialPopulator {

    private final LinkRepository repository;

    public static void main(String[] args) {
        RedisTwoDBLinkRepositoryImpl repository=new RedisTwoDBLinkRepositoryImpl();
        new InitialPopulator(repository).init();

    }

    public InitialPopulator(LinkRepository repository) {
        this.repository = repository;
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

    private void init() {
        repository.init();
        BigInteger keyCount=repository.createKeys(1);
        System.out.println(String.format("'%s' ключей добавлено",keyCount.toString()));
    }

}
