package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import com.lambdaworks.redis.ScoredValue;

import java.io.IOException;
import java.util.List;

/**
 * Created by Ivan on 01.01.2017.
 */

public interface Service {

    String getRandomShortLink();

    String create(String link);

    String get(String shortLink);

    List<List<String>> getShortStat();

    List<List<String>> getFullStat();

    void createQRImage(String path,String link,String shortLink) throws WriterException,
            IOException;
}
