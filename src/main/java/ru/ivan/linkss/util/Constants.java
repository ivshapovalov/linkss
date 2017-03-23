package ru.ivan.linkss.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Constants {

    public static  final boolean DEBUG=true;

    public static final String PROPERTIES_PATH = System.getenv("LINKSS_PROPERTIES_FILE");

    static final Properties appProperties;

    static {
        appProperties = new Properties();
        try (InputStream in = new FileInputStream(PROPERTIES_PATH)) {
            appProperties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String REDIS_URL = appProperties.getProperty("redis.url");
    public static final String GEOIP_URL = appProperties.getProperty("geoip.url");
    public static final String GEOIP_KEY = appProperties.getProperty("geoip.key");
    public static final String GOOGLE_RECAPTCHA_VERIFY_URL = appProperties.getProperty("google.recaptcha.verify.url");
    public static final String GOOGLE_RECAPTCHA_VERIFY_KEY = appProperties.getProperty("google" +
            ".recaptcha.verify.key");

}
