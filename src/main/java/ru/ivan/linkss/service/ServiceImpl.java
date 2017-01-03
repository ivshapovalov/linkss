package ru.ivan.linkss.service;

import ru.ivan.linkss.repository.LinkRepository;
import ru.ivan.linkss.repository.RedisLinkRepositoryImpl;


public class ServiceImpl implements Service {

    LinkRepository repository=new RedisLinkRepositoryImpl();

    public ServiceImpl() {
    }

    @Override
    public String getRandomShortLink() {
        return repository.getRandomShortLink();
    }

    @Override
    public String create(String link) {
        return repository.create(link);
    }

    @Override
    public String get(String shortLink) {
        return repository.get(shortLink);
    }
}
