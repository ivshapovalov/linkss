package ru.ivan.linkss.repository;

import java.util.List;

/**
 * Created by Ivan on 01.01.2017.
 */
public interface LinkRepository {
    String create(String link);

    String get(String key);

    String getRandomShortLink();

    List<List<String>> getShortStat();
    List<List<String>> getFullStat();
}
