package ru.ivan.linkss.controller;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Controller
@EnableScheduling
public class RootController {


    @Autowired
    private LinksService service;

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

        String link = service.visitLink(shortLink.substring(1));
        if (link.contains(":")) {
            return "redirect:" + link;
        } else {
            return "redirect:" + "//" + link;
        }
    }

    @RequestMapping(value = "/resources/*.png", method = RequestMethod.GET)
    public void openImage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String shortLink = request.getServletPath();
        OutputStream os = response.getOutputStream();
        File file = new File(request.getServletContext().getRealPath(shortLink));
        FileInputStream fis = new FileInputStream(file);
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
        String shortLink = "";
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            shortLink = service.createShortLink(null, link);
        } else {
            shortLink = service.createShortLink(autorizedUser, link);
        }
        if (shortLink == null) {
            model.addAttribute("message", "Sorry, free short links ended. Try later!");
            return "error";
        }
        String path = request.getServletContext().getRealPath("/");
        try {
            service.createQRImage(path, shortLink, request.getRequestURL() + shortLink);

        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }
        model.addAttribute("user", autorizedUser);
        model.addAttribute("image", "/resources/" + shortLink + ".png");
        model.addAttribute("link", link);
        model.addAttribute("shortLink", request.getRequestURL() + shortLink);

        return "main";
    }

    //    @RequestMapping(value = "/actions/statistics", method = RequestMethod.GET)
//    public String statistics(Model model,
//                             HttpServletRequest request, HttpSession session) {
//
//        User autorizedUser = (User) session.getAttribute("autorizedUser");
//        if (autorizedUser == null || autorizedUser.isEmpty()) {
//            model.addAttribute("message", "Sorry, statistics available only for logged users!");
//            return "error";
//        }
//        if (autorizedUser.isAdmin()) {
//            List<List<String>> shortStat = service.getShortStat();
//            String contextPath = getContextPath(request);
//            List<FullLink> fullStat = service.getFullStat(contextPath);
//            model.addAttribute("shortStat", shortStat);
//            model.addAttribute("fullStat", fullStat);
//        } else {
//            String contextPath = getContextPath(request);
//            List<FullLink> fullStat = service.getFullStat(autorizedUser.getUserName(),
//                    contextPath,1,1);
//            model.addAttribute("fullStat", fullStat);
//        }
//        return "statistics";
//    }
//


}
