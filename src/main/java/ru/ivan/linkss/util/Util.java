package ru.ivan.linkss.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Ivan on 03.01.2017.
 */
public class Util {

    public static String getDomainName(String url) {
        URI uri = null;
        try {
            if (url.contains("://")) {
                uri = new URI(url);
            } else {
                uri = new URI("http://" + url);
            }
            String domain = uri.getHost();
            return domain.startsWith("www") ? domain.substring(domain.indexOf(".")+1) : domain;
        } catch (URISyntaxException e) {
            return "";
        }
    }
}
