package ru.ivan.linkss.controller;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.FullLink;
import ru.ivan.linkss.repository.User;
import ru.ivan.linkss.service.LinkssService;

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
public class MainController {

    private static final String DEFAULT_USER = "user";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_DELETE = "delete";

    @Autowired
    private LinkssService service;

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

        String link = service.getLink(shortLink.substring(1));
        if (link.contains(":")) {
            return "redirect:" + link;
        } else {
            return "redirect:" + "//" + link;
        }
    }

    @RequestMapping(value = "*.png", method = RequestMethod.GET)
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
    }

    @RequestMapping(value = "/actions/signup", method = RequestMethod.GET)
    public String registration(Model model, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        model.addAttribute("user", new User());
        return "signup";
    }

    @RequestMapping(value = "/actions/login", method = RequestMethod.GET)
    public String signin(Model model)
            throws IOException {
        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping(value = "/actions/logout", method = RequestMethod.GET)
    public String logout(Model model, HttpSession session)
            throws IOException {
        model.addAttribute("autorizedUser", null);
        session.setAttribute("autorizedUser", null);
        return "redirect:/";
    }

    @RequestMapping(value = "/actions/manage", method = {RequestMethod.GET, RequestMethod.POST})
    public String manage(Model model, HttpSession session)
            throws IOException {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser != null && !autorizedUser.isEmpty() && autorizedUser.isAdmin()) {
            List<User> users = service.getUsers();
            model.addAttribute("autorizedUser", autorizedUser);
            model.addAttribute("users", users);
            return "manage";
        }
        return "main";
    }

    @RequestMapping(value = "/actions/register", method = RequestMethod.POST)
    public String register(Model model,
                           @ModelAttribute("user") User user,
                           HttpServletRequest request,
                           HttpSession session) {
        try {
            service.createUser(user.getUserName(), user.getPassword());
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
        if (request.getParameter("register") != null) {
            return "redirect:/";
        } else if (request.getParameter("registerAndLogin") != null) {
            return autoLogin(model, user, session);
        }
        return "redirect:/";

    }

    @RequestMapping(value = "/actions/login", method = RequestMethod.POST)
    public String login(Model model,
                        @ModelAttribute("user") User user,
                        HttpSession session) {
        if (user != null && !user.getUserName().equals("") && !user.getPassword().equals("")) {
            try {
                return autoLogin(model, user, session);
            } catch (RuntimeException e) {
                model.addAttribute("message", e.getMessage());
                return "error";
            }
        }
        return "login";
    }

    private String autoLogin(Model model, User user, HttpSession session) {
        boolean existedUser = service.checkUser(user);
        if (existedUser) {
            user.setEmpty(false);
            if (user.getUserName().equals("admin")) {
                user.setAdmin(true);
            }
            session.setAttribute("autorizedUser", user);
            model.addAttribute("autorizedUser", user);
            return "redirect:/";
        }
        return "signin";
    }

    @RequestMapping(value = {"/actions/deletelink"}, method =
            RequestMethod.GET)
    public String deletelink(Model model,
                             @ModelAttribute("key") String shortLink,
                             @ModelAttribute("owner") String owner,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "User is not defined!");
            return "error";
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "Link owner is not defined!");
            return "error";
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute("message", "Link is not defined!");
            return "error";
        }

        try {
            service.deleteUserLink(autorizedUser, shortLink, owner);
            model.addAttribute("key", null);
            model.addAttribute("owner", null);

            return "redirect:/actions/statistics";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/actions/user/{owner}/links/delete"}, method =
            RequestMethod.GET)
    public String deleteuserlink(Model model,
                                 @ModelAttribute("key") String shortLink,
                                 @ModelAttribute("owner") String owner,
                                 HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "User is not defined!");
            return "error";
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "Link owner is not defined!");
            return "error";
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute("message", "Link is not defined!");
            return "error";
        }

        try {
            service.deleteUserLink(autorizedUser, shortLink, owner);
            model.addAttribute("key", null);
            model.addAttribute("owner", null);

            return "redirect:/actions/links?owner=" + owner;
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/actions/user/{owner}/links/edit"}, method =
            RequestMethod.GET)
    public String updateuserlink(Model model,
                                 @ModelAttribute("key") String shortLink,
                                 @ModelAttribute("owner") String owner,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "User is not defined!");
            return "error";
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "Link owner is not defined!");
            return "error";
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute("message", "Link is not defined!");
            return "error";
        }

        try {
            String link=service.getLink(shortLink);
            String contextPath=getContextPath(request);
            FullLink fullLink=new FullLink(shortLink,contextPath+shortLink,link,
                    "",contextPath+shortLink+".png",
                    owner,service.getLinkDays(shortLink));
            model.addAttribute("fullLink", fullLink);
            model.addAttribute("oldFullLink", fullLink);
            model.addAttribute("owner", owner);
            return "link";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/actions/users"}, method =
            RequestMethod.GET)
    public String editUser(Model model,
                           @ModelAttribute("key") String key,
                           @ModelAttribute("action") String action,
                           HttpSession session) {
        if (action == null || action.equals("")) {
            model.addAttribute("message", "Action is not defined!");
            return "error";
        }
        if (ACTION_EDIT.equalsIgnoreCase(action)) {
            return actionEditUser(model, key, session);
        } else if (ACTION_DELETE.equalsIgnoreCase(action)) {
            return actionDeleteUser(model, key, session);

        }
        return "manage";
    }

    private String actionEditUser(Model model, String key, HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }

        if (key == null || key.equals("")) {
            model.addAttribute("message", "User name is not defined!");
            return "error";
        }
        try {
            User user = service.getUser(autorizedUser, key);
            model.addAttribute("user", user);
            model.addAttribute("oldUserName", user.getUserName());
            model.addAttribute("oldPassword", user.getPassword());
            return "user";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    private String actionDeleteUser(Model model, String key, HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }

        if (key == null || key.equals("")) {
            model.addAttribute("message", "User name is not defined!");
            return "error";
        }
        try {
            service.deleteUser(autorizedUser, key);
            model.addAttribute("action",null);
            model.addAttribute("key",null);
            return "redirect:/actions/manage";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/actions/user"}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute("user") User newUser,
                             @ModelAttribute("oldUserName") String oldUserName,
                             @ModelAttribute("oldPassword") String oldPassword,
                             HttpServletRequest request,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }
        if (newUser == null) {
            model.addAttribute("message", "User for update is not defined!");
            return "error";
        }
        try {
            User oldUser = new User(oldUserName, oldPassword);
            service.updateUser(autorizedUser, newUser, oldUser);
            model.addAttribute("oldUserName", null);
            model.addAttribute("oldPassword", null);

            return "redirect:/actions/manage";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/actions/link"}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute("fullLink") FullLink fullLink,
                             @ModelAttribute("oldFullLink") FullLink oldFullLink,
                             @ModelAttribute("owner") String owner,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "Link owner is not defined!");
            return "error";
        }
        if (fullLink == null) {
            model.addAttribute("message", "Link is not defined!");
            return "error";
        }
        try {
            //TODO сравнить сущности, произвести действия
            String message=updateLink(oldFullLink,fullLink);
            return "redirect:links";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShortLink(Model model, HttpSession session,
                                  HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        String link = request.getParameter("link");
        if ("".equals(link)) {
            return "main";
        }
        String shortLink = "";
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            shortLink = service.createShortLink(DEFAULT_USER, link);
        } else {
            shortLink = service.createShortLink(autorizedUser.getUserName(), link);
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
        model.addAttribute("filename", shortLink + ".png");
        model.addAttribute("link", link);
        model.addAttribute("shortLink", request.getRequestURL() + shortLink);

        return "main";
    }

    @RequestMapping(value = "/actions/statistics", method = RequestMethod.GET)
    public String statistics(Model model,
                             HttpServletRequest request, HttpSession session) {

        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, statistics available only for logged users!");
            return "error";
        }
        if (autorizedUser.isAdmin()) {
            List<List<String>> shortStat = service.getShortStat();
            String contextPath = getContextPath(request);
            List<FullLink> fullStat = service.getFullStat(contextPath);
            model.addAttribute("shortStat", shortStat);
            model.addAttribute("fullStat", fullStat);
        } else {
            String contextPath = getContextPath(request);
            List<FullLink> fullStat = service.getFullStat(autorizedUser.getUserName(),
                    contextPath,1,1);
            model.addAttribute("fullStat", fullStat);
        }
        return "statistics";
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

    @RequestMapping(value = "/actions/links", method = RequestMethod.GET)
    public String links(Model model,
                        @ModelAttribute("owner") String owner,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, statistics available only for logged users!");
            return "error";
        }

        if (request.getParameter("page") != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        String contextPath = getContextPath(request);
        int offset=(currentPage-1) * recordsOnPage;
        List<FullLink> list = service.getFullStat(owner, contextPath,offset,recordsOnPage);
        long productCount = (int) service.getUserLinksSize(autorizedUser,owner);
        if (productCount == 0) {
            model.addAttribute("message", "Sorry, User don't have links. Try another!");
            return "error";
        }
        int numberOfPages = (int) Math.ceil(productCount / recordsOnPage);

        model.addAttribute("list", list);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("productCount", productCount);
        model.addAttribute("owner", owner);


        return "links";
    }

    public String updateLink(FullLink oldFullLink, FullLink newFullLink) {
        String newKey=newFullLink.getKey();
        String oldKey=newFullLink.getKey();
        StringBuilder message=new StringBuilder();
        if(oldKey==null || "".equals(oldKey)) {
            message.append("Updated link has empty key").append("\r\n");
        }
        if(newKey==null || "".equals(newKey)) {
            message.append("New link has empty key").append("\r\n");
        }
        if(oldFullLink.getUserName()==null || "".equals(oldFullLink.getUserName())) {
            message.append("Updated link has empty user").append("\r\n");
        }
        if(newFullLink.getUserName()==null || "".equals(newFullLink.getUserName())) {
            message.append("New link has empty user").append("\r\n");
        }
        if (message.equals("")) {
            return message.toString();
        }
        if(newKey.equals(oldKey)) {
            message.append("New link has empty key").append("\r\n");
            //service.updateLink(newFullLink);
        } else {
            //service.updateLink(newFullLink);

        }

        return message.toString();

    }

}
