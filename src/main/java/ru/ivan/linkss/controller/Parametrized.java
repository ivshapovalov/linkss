package ru.ivan.linkss.controller;


import ru.ivan.linkss.util.Constants;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public interface Parametrized {

    default Map<String, String> getParameters(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        String userAgent=request.getHeader("user-agent");
        String ip = request.getRemoteAddr();
        if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String ipAddress = inetAddress.getHostAddress();
            ip = ipAddress;
        }

        if (Constants.DEBUG) {
            ip = Populator.getRandomIp();
            userAgent = Populator.getRandomUserAgent();
        }
        map.put("user-agent", userAgent);
        map.put("ip", ip);

        return map;
    }
}
