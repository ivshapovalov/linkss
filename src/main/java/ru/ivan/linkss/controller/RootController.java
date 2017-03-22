package ru.ivan.linkss.controller;


import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.RepositoryException;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.util.VerifyRecaptcha;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.ivan.linkss.util.Constants.DEBUG;

@Controller
@EnableScheduling
public class RootController {

    @Autowired
    LinksService service;

    private static final String FILE_SEPARTOR = File.separator;
    private static final String WEB_SEPARTOR = "/";
    private static final String IMAGE_EXTENSION_WITH_DOT = ".png";
    private static final String ICON_EXTENSION_WITH_DOT = ".ico";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_MESSAGE = "message";
    private static final String PAGE_MAIN = "main";
    private static final String PAGE_INIT = "init";

    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_USER = "user";
    private static final String ATTRIBUTE_SHORTLINK = "shortLink";
    private static final String ATTRIBUTE_LINK = "link";
    private static final String ATTRIBUTE_IMAGE = "image";


    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\" +
            ".[a-z0-9-]+)+([/?].*)?$";

    @RequestMapping(value = {WEB_SEPARTOR, WEB_SEPARTOR + PAGE_MAIN}, method = RequestMethod.GET)
    public String main(Model model,
                       HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && autorizedUser.isVerified()) {
            model.addAttribute(ATTRIBUTE_AUTORIZED_USER, autorizedUser);
        }
        return PAGE_MAIN;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_INIT}, method = RequestMethod.GET)
    public String captcha(HttpServletRequest request) {
        return PAGE_INIT;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_INIT}, method = RequestMethod.POST)
    public String init(Model model, HttpServletRequest request) {
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        boolean valid;
        if (DEBUG) {
            valid = true;
        } else {
            valid = VerifyRecaptcha.verify(gRecaptchaResponse);
        }
        String errorString = null;
        if (!valid) {
            errorString = "Captcha invalid!";
        }
        if (!valid) {
            model.addAttribute(ATTRIBUTE_MESSAGE, errorString);
            return PAGE_ERROR;
        } else {
            String path = request.getServletContext().getRealPath(WEB_SEPARTOR);
            service.clear(path);
            return "redirect:./";
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + "*", method = RequestMethod.GET)
    public String redirect(Model model, HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String shortLink = servletPath.substring(servletPath.lastIndexOf(WEB_SEPARTOR) + 1);
        Map<String,String> params = getIP(request);
        String link = service.visitLink(shortLink, params);
        if (link != null) {
            Pattern p = Pattern.compile(URL_REGEX);
            Matcher m = p.matcher(link);
            if (!m.find()) {
                model.addAttribute(ATTRIBUTE_MESSAGE, link);
                return PAGE_MESSAGE;

            } else {
                if (link.contains(":")) {
                    return "redirect:" + link;
                } else {
                    return "redirect:" + "//" + link;
                }
            }
        }
        model.addAttribute("message", String.format("Link '%s' does not exist", shortLink));
        return PAGE_ERROR;
    }

    @RequestMapping(value = WEB_SEPARTOR + "*" + IMAGE_EXTENSION_WITH_DOT, method =
            RequestMethod.GET)
    public void openImage(Model model, HttpServletRequest request, HttpServletResponse response
    )
            throws IOException {
        String shortLink = request.getServletPath();
        String key = shortLink.substring(shortLink.lastIndexOf(WEB_SEPARTOR) + 1, shortLink.lastIndexOf
                ("."));

        String link = service.getLink(key).getLink();
        if (link != null) {
            String filePath = request.getServletContext().getRealPath("") +
                    "resources" + FILE_SEPARTOR + "qr" +
                    FILE_SEPARTOR + key + IMAGE_EXTENSION_WITH_DOT;
            File imageOnDisk = new File(filePath);
            boolean created = true;
            if (!imageOnDisk.exists()) {
//                created = service.downloadImageFromFTP(key + IMAGE_EXTENSION_WITH_DOT, filePath);
                created = false;
            }
            if (!created) {
                String context = getContextPath(request);
                String shortLinkPath = context + key;
                try {
                    service.createQRImage(filePath, key, shortLinkPath);
                    //service.uploadImageToFTP(filePath, key);
                    created = true;
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (created) {
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(imageOnDisk);
                int bytes;
                while ((bytes = fis.read()) != -1) {
                    os.write(bytes);
                }
                fis.close();
            } else {
                response.setContentType("text/plain");
                response.getWriter().write(String.format("Sorry. Image '%s' does not exists", key));

            }
        } else {
            response.setContentType("text/plain");
            response.getWriter().write(String.format("Sorry. Link '%s' does not exists", key));
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + "{" + ATTRIBUTE_SHORTLINK + "}" + ICON_EXTENSION_WITH_DOT, method =
            RequestMethod
                    .GET)
    public void openIcon(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String shortLink = request.getServletPath();
        OutputStream os = response.getOutputStream();
        String filePath = request.getServletContext().getRealPath("") + "resources" + FILE_SEPARTOR + "images" +
                FILE_SEPARTOR + "favicon.ico";
        File imageOnDisk = new File(filePath);
        FileInputStream fis = new FileInputStream(imageOnDisk);
        int bytes;
        while ((bytes = fis.read()) != -1) {
            os.write(bytes);
        }
        fis.close();
    }

    @RequestMapping(value = WEB_SEPARTOR, method = RequestMethod.POST)
    public String createShortLink(Model model, HttpSession session,
                                  HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Map<String,String> params = getIP(request);

        String link = request.getParameter(ATTRIBUTE_LINK);
        if (link == null || "".equals(link)) {
            return PAGE_MAIN;
        }
        link = link.trim();
        String path = request.getServletContext().getRealPath("/");
        String context = request.getRequestURL().toString();
        String shortLink = "";
        try {
            if (autorizedUser == null || !autorizedUser.isVerified()) {
                shortLink = service.createShortLink(null, link, path, context,params);
            } else {
                shortLink = service.createShortLink(autorizedUser, link, path, context,params);
            }
        } catch (RepositoryException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
        if (shortLink == null) {
            model.addAttribute("message", "Sorry, free short links ended. Try later!");
            return PAGE_ERROR;
        }
        model.addAttribute(ATTRIBUTE_USER, autorizedUser);
        model.addAttribute(ATTRIBUTE_IMAGE, FILE_SEPARTOR + shortLink + IMAGE_EXTENSION_WITH_DOT);
        model.addAttribute(ATTRIBUTE_LINK, link);
        model.addAttribute(ATTRIBUTE_SHORTLINK, request.getRequestURL() + shortLink);
        return PAGE_MAIN;
    }

    private String getContextPath(HttpServletRequest request) {
        String p = request.getRequestURL().toString();
        String cp = request.getServletPath();

        String contextPath = "";
        if (p.endsWith(cp)) {
            contextPath = p.substring(0, p.length() - cp.length() + 1);
        }
        return contextPath;
    }

    private Map<String, String> getIP(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
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
        ip = "25.25.25.25";
        map.put("ip", ip);
        map.put("user-agent", request.getHeader("user-agent"));

        return map;
    }
}


