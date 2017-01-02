package ru.ivan.linkss.repository;

/**
 * Created by Ivan on 01.01.2017.
 */
public interface LinkRepository {
    String create(String link);

    String get(String key);
}
