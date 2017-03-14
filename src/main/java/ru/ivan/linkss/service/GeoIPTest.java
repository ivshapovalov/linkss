package ru.ivan.linkss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.corn.httpclient.HttpClient;
import net.sf.corn.httpclient.HttpResponse;
import ru.ivan.linkss.repository.entity.IPLocation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GeoIPTest {

    public static void main(String[] args) throws URISyntaxException, IOException {

        String ip = "46.188.121.42";
//        String geoIpServer = "http://app.whydt.ru:49193/geoip/";
        String geoIpServer = System.getenv("GEOIP_URL");
        HttpClient client = new HttpClient(new URI(geoIpServer + ip));
        HttpResponse response = client.sendData(HttpClient.HTTP_METHOD.GET);
        if (!response.hasError()) {
            String jsonString = response.getData();
            IPLocation location = null;
            try {
                location = new ObjectMapper().readValue(jsonString, IPLocation
                        .class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(location);
        }
    }

}
