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

@Controller
@EnableScheduling
public class RootController {

    @Autowired
    private LinksService service;

    private final String fileSepartor=File.separator;
    private final String webSepartor="/";

    @RequestMapping(value = {"/", "/main"}, method = RequestMethod.GET)
    public String main(Model model,
                       HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser != null && !autorizedUser.isEmpty()) {
            model.addAttribute("autorizedUser", autorizedUser);
        }
        return "main";
    }

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String redirect(HttpServletRequest request) {
        String shortLink = request.getServletPath();

        String link = service.visitLink(shortLink.substring(shortLink.lastIndexOf(webSepartor) + 1));
        if (link.contains(":")) {
            return "redirect:" + link;
        } else {
            return "redirect:" + "//" + link;
        }
    }

    @RequestMapping(value = "/*.png", method = RequestMethod.GET)
    public void openImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String shortLink = request.getServletPath();
        OutputStream os = response.getOutputStream();
        String key = shortLink.substring(shortLink.lastIndexOf(webSepartor) + 1, shortLink.lastIndexOf
                ("."));
        String filePath = request.getServletContext().getRealPath("")+"resources"+fileSepartor+shortLink;
        File imageOnDisk = new File(filePath);
        if (!imageOnDisk.exists()) {
            service.downloadImageFromS3(filePath, key);
        }
        FileInputStream fis = new FileInputStream(imageOnDisk);
        int bytes;
        while ((bytes = fis.read()) != -1) {
            os.write(bytes);
        }
        fis.close();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShortLink(Model model, HttpSession session,
                                  HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        String link = request.getParameter("link");
        if (link == null || "".equals(link)) {
            return "main";
        }
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
            return "error";
        }

        model.addAttribute("user", autorizedUser);
        model.addAttribute("image", fileSepartor + shortLink + ".png");
        //model.addAttribute("image", link);
        model.addAttribute("link", link);
        model.addAttribute("shortLink", request.getRequestURL() + shortLink);

        return "main";
    }


}


