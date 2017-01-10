package ru.ivan.linkss.repository;

import java.util.List;

public interface LinkRepository {
    String create(String link);

    String get(String key);

    String getRandomShortLink();

    List<List<String>> getShortStat();

    List<FullLink> getFullStat(String contextPath);
}
