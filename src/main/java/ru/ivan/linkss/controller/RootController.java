package ru.ivan.linkss.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_USER = "user";
    private static final String ATTRIBUTE_SHORTLINK = "shortLink";
    private static final String ATTRIBUTE_LINK = "link";
    private static final String ATTRIBUTE_IMAGE = "image";


    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\" +
            ".[a-z0-9-]+)+([/?].*)?$";

    @RequestMapping(value = {WEB_SEPARTOR, WEB_SEPARTOR+ PAGE_MAIN}, method = RequestMethod.GET)
    public String main(Model model,
                       HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && !autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_AUTORIZED_USER, autorizedUser);
        }
        return PAGE_MAIN;
    }

    @RequestMapping(value = WEB_SEPARTOR+"*", method = RequestMethod.GET)
    public String redirect(Model model,HttpServletRequest request) {
        String shortLink = request.getServletPath();

        String link = service.visitLink(shortLink.substring(shortLink.lastIndexOf(WEB_SEPARTOR) + 1));
        if (link != null) {
            Pattern p = Pattern.compile(URL_REGEX);
            Matcher m = p.matcher(link);
            if(!m.find()) {
                model.addAttribute(ATTRIBUTE_MESSAGE, link);
                return PAGE_MESSAGE;

            }else {
                if (link.contains(":")) {
                    return "redirect:" + link;
                } else {
                    return "redirect:" + "//" + link;
                }
            }
        }
        return PAGE_ERROR;
    }

    @RequestMapping(value = WEB_SEPARTOR+"*"+ IMAGE_EXTENSION_WITH_DOT, method = RequestMethod.GET)
    public void openImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String shortLink = request.getServletPath();
        OutputStream os = response.getOutputStream();
        String key = shortLink.substring(shortLink.lastIndexOf(WEB_SEPARTOR) + 1, shortLink.lastIndexOf
                ("."));
        String filePath = request.getServletContext().getRealPath("") + "resources" + FILE_SEPARTOR + shortLink;
        File imageOnDisk = new File(filePath);
        if (!imageOnDisk.exists()) {
            service.downloadImageFromFTP(filePath, key);
        }
        FileInputStream fis = new FileInputStream(imageOnDisk);
        int bytes;
        while ((bytes = fis.read()) != -1) {
            os.write(bytes);
        }
        fis.close();
    }

    @RequestMapping(value = WEB_SEPARTOR+"*"+ ICON_EXTENSION_WITH_DOT, method = RequestMethod.GET)
    public void openIcon(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String shortLink = request.getServletPath();
        OutputStream os = response.getOutputStream();
        String filePath = request.getServletContext().getRealPath("") + "resources"+FILE_SEPARTOR+"images" +
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

        String link = request.getParameter(ATTRIBUTE_LINK);
        if (link == null || "".equals(link)) {
            return PAGE_MAIN;
        }
        link=link.trim();
        String path = request.getServletContext().getRealPath("/");
        String context = request.getRequestURL().toString();
        String shortLink = "";
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            shortLink = service.createShortLink(null, link, path, context);
        } else {
            shortLink = service.createShortLink(autorizedUser, link, path, context);
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
}


