package ru.ivan.linkss.controller;


import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.util.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;

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

        String link = service.visitLink(shortLink.substring(shortLink.lastIndexOf("/") + 1));
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
        String filePath = request.getServletContext().getRealPath(shortLink);
        String key=shortLink.substring(shortLink.lastIndexOf("/")+1,shortLink.lastIndexOf("."));
        service.downloadImageFromS3(filePath,key);

        File file = new File(filePath);
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
        String imagePath=path+"resources//" +shortLink + ".png";

        service.uploadImage(imagePath, shortLink, request.getRequestURL() + shortLink);
//            service.createQRImage(imagePath, shortLink, request.getRequestURL() + shortLink);
//            service.sendFileToS3(imagePath,shortLink);

        model.addAttribute("user", autorizedUser);
        model.addAttribute("image", "/"+shortLink + ".png");
        //model.addAttribute("image", link);
        model.addAttribute("link", link);
        model.addAttribute("shortLink", request.getRequestURL() + shortLink);

        return "main";
    }


}


//        AmazonS3 s3client  = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//        java.util.Date expiration = new java.util.Date();
//        long msec = expiration.getTime();
//        msec += 1000 * 60 * 60; // 1 hour.
//        expiration.setTime(msec);
//
//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest(S3_BUCKET_NAME, AWS_ACCESS_KEY_ID);
//        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
//        generatePresignedUrlRequest.setExpiration(expiration);
//
//        return s3client.generatePresignedUrl(generatePresignedUrlRequest).toString();

//}


