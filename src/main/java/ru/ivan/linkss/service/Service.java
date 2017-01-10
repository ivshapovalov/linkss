package ru.ivan.linkss.service;

import com.google.zxing.WriterException;
import ru.ivan.linkss.repository.FullLink;

import java.io.IOException;
import java.util.List;

public interface Service {

    String getRandomShortLink();

    String create(String link);

    String get(String shortLink);

    List<List<String>> getShortStat();

    List<FullLink> getFullStat(String contextPath);

    void createQRImage(String path,String link,String shortLink) throws WriterException,
            IOException;
}
