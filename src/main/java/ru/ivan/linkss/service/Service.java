package ru.ivan.linkss.service;

import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Ivan on 01.01.2017.
 */

public interface Service {

    String getRandomShortLink();

    String create(String link);

    String get(String shortLink);

    void createQRImage(String path,String link,String shortLink) throws WriterException,
            IOException;
}
