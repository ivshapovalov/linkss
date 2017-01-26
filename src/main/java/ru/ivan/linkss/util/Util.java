package ru.ivan.linkss.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Util {

    public static String getDomainName(String url) throws NullPointerException {
        URI uri = null;
        try {
            if (url.contains("://")) {
                uri = new URI(url);
            } else {
                uri = new URI("http://" + url);
            }
            String domain = uri.getHost();
            if (domain==null) domain="";
            return domain.startsWith("www") ? domain.substring(domain.indexOf(".")+1) : domain;
        } catch (URISyntaxException e) {
            System.out.println("Ошибка:" +url);

            return "";
        }
    }
}
