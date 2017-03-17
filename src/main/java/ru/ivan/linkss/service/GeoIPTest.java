package ru.ivan.linkss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.corn.httpclient.HttpClient;
import net.sf.corn.httpclient.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.ivan.linkss.repository.entity.IpLocation;
import ru.ivan.linkss.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GeoIPTest {

    public static void main(String[] args) throws URISyntaxException, IOException {

        String ip="25.25.25.25";
        String jsonString="";
        //String geoIpServer = System.getenv("GEOIP_URL");
        HttpClient client = new HttpClient(new URI(Constants.GEOIP_URL + ip));
        HttpResponse response = client.sendData(HttpClient.HTTP_METHOD.GET);
        if (!response.hasError()) {
            jsonString = response.getData();
            IpLocation location = null;
            try {
                location = new ObjectMapper().readValue(jsonString, IpLocation
                        .class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(location);
        }
    }

}
