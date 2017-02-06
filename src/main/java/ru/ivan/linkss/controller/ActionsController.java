package ru.ivan.linkss.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.entity.Domain;
import ru.ivan.linkss.repository.entity.FullLink;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.service.Populator;
import ru.ivan.linkss.util.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Controller
@EnableScheduling
@RequestMapping(value = "/actions")
public class ActionsController {

    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_DELETE = "delete";

    private static final String FILE_SEPARTOR = File.separator;
    private static final String WEB_SEPARTOR = "/";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_MAIN = "main";
    private static final String PAGE_SIGNUP = "signup";
    private static final String PAGE_SIGNIN = "signin";
    private static final String PAGE_MANAGE = "manage";
    private static final String PAGE_USERS = "users";
    private static final String ACTION_LOGOUT = "logout";

    private static final String ATTRIBUTE_USER = "user";
    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_PAGE = "page";

    @Autowired
    private LinksService service;

    @RequestMapping(value = WEB_SEPARTOR+PAGE_SIGNUP, method = RequestMethod.GET)
    public String registration(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User());
        return PAGE_SIGNUP;
    }

    @RequestMapping(value = WEB_SEPARTOR+PAGE_SIGNIN, method = RequestMethod.GET)
    public String signin(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User());
        return PAGE_SIGNIN;
    }

    @RequestMapping(value = WEB_SEPARTOR+ACTION_LOGOUT, method = RequestMethod.GET)
    public String logout(Model model, HttpSession session)
            throws IOException {
        model.addAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        session.setAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        return "redirect:/";
    }

    @RequestMapping(value = WEB_SEPARTOR+PAGE_MANAGE, method = {RequestMethod.GET, RequestMethod.POST})
    public String manage(HttpSession session)
            throws IOException {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && !autorizedUser.isEmpty() && autorizedUser.isAdmin()) {
            return PAGE_MANAGE;
        }
        return PAGE_MAIN;
    }

    @RequestMapping(value = WEB_SEPARTOR+PAGE_USERS, method = {RequestMethod.GET, RequestMethod.POST})
    public String users(Model model, HttpSession session, HttpServletRequest request)
            throws IOException {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && !autorizedUser.isEmpty() && !autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, users available only for logged admin users!");
            return PAGE_ERROR;
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter(ATTRIBUTE_PAGE));
        }

        int offset = (currentPage - 1) * recordsOnPage;
        List<User> users = service.getUsers(offset, recordsOnPage);
        long usersCount = (int) service.getUsersSize(autorizedUser);
        if (usersCount == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, DB don't have users. Try later!");
            return PAGE_ERROR;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) usersCount / recordsOnPage));

        model.addAttribute("users", users);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("autorizedUser", autorizedUser);
        return PAGE_USERS;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model,
                           @ModelAttribute(ATTRIBUTE_USER) User user,
                           HttpServletRequest request,
                           HttpSession session) {
        try {
            service.createUser(user.getUserName(), user.getPassword());
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
        if (request.getParameter("register") != null) {
            return "redirect:/";
        } else if (request.getParameter("registerAndLogin") != null) {
            return autoLogin(model, user, session);
        }
        return "redirect:/";

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Model model,
                        @ModelAttribute(ATTRIBUTE_USER) User user,
                        HttpSession session) {
        if (user != null && user.getUserName() != null && !user.getUserName().equals("")
                && user.getPassword() != null && !user.getPassword().equals("")) {
            try {
                return autoLogin(model, user, session);
            } catch (RuntimeException e) {
                model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
                return PAGE_ERROR;
            }
        }
        return "signin";
    }

    private String autoLogin(Model model, User user, HttpSession session) {
        boolean existedUser = service.checkUser(user);
        if (existedUser) {
            user.setEmpty(false);
            if ("admin".equals(user.getUserName())) {
                user.setAdmin(true);
            }
            session.setAttribute(ATTRIBUTE_AUTORIZED_USER, user);
            model.addAttribute(ATTRIBUTE_AUTORIZED_USER, user);
            return "redirect:/";
        }
        return "signin";
    }

    @RequestMapping(value = {"/deletelink"}, method =
            RequestMethod.GET)
    public String deletelink(Model model,
                             @ModelAttribute("key") String shortLink,
                             @ModelAttribute("owner") String owner,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName() == null
                || autorizedUser.getUserName().equals("")) {
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

    @RequestMapping(value = {"/user/{owner}/links/delete"}, method =
            RequestMethod.GET)
    public String deleteuserlink(Model model,
                                 @ModelAttribute("key") String shortLink,
                                 @ModelAttribute("owner") String owner,
                                 HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
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

    @RequestMapping(value = {"/user/{owner}/links/edit"}, method =
            RequestMethod.GET)
    public String updateuserlink(Model model,
                                 @ModelAttribute("key") String shortLink,
                                 @ModelAttribute("owner") String owner,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
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
            String realImagePath = request.getServletContext().getRealPath("")
                    + "resources"+FILE_SEPARTOR + shortLink + ".png";
            File imageOnDisk = new File(realImagePath);
            if (!imageOnDisk.exists()) {
                Util.downloadImageFromS3(realImagePath, shortLink);
            }

            String link = service.getLink(shortLink);
            String contextPath = getContextPath(request);
            String urlImagePath = contextPath + shortLink + ".png";
            FullLink fullLink = new FullLink(shortLink, contextPath + shortLink, link,
                    "", urlImagePath,
                    owner, service.getLinkDays(shortLink));
            model.addAttribute("fullLink", fullLink);
            model.addAttribute("oldKey", shortLink);
            model.addAttribute("owner", owner);
            return "link";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/users/{owner}/clear"}, method =
            RequestMethod.GET)
    public String clearUser(Model model,
                           @PathVariable("owner") String owner,
                           HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        if (autorizedUser == null || "".equals(autorizedUser.getUserName())) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }

        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "User name is not defined!");
            return "error";
        }
        try {
            service.clearUser(autorizedUser, owner);
            model.addAttribute("owner", null);
            return "redirect:/actions/users";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }
    @RequestMapping(value = {"/users/{owner}/edit"}, method =
            RequestMethod.GET)
    private String editUser(Model model, @PathVariable("owner") String key
            , HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        if (autorizedUser == null || autorizedUser.getUserName() == null || "".equals(autorizedUser.getUserName())) {
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

    @RequestMapping(value = {"/users/{owner}/delete"}, method =
            RequestMethod.GET)
    private String deleteUser(Model model, @PathVariable("owner") String owner, HttpSession
            session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");

        if (autorizedUser == null || "".equals(autorizedUser.getUserName())) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }

        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "User name is not defined!");
            return "error";
        }
        try {
            service.deleteUser(autorizedUser, owner);
            model.addAttribute("action", null);
            model.addAttribute("owner", null);
            return "redirect:/actions/users";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = {"/user"}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute("user") User newUser,
                             @ModelAttribute("oldUserName") String oldUserName,
                             @ModelAttribute("oldPassword") String oldPassword,
                             HttpServletRequest request,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
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

    @RequestMapping(value = {"/link"}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute("fullLink") FullLink fullLink,
                             @ModelAttribute("oldKey") String shortLink,
                             @ModelAttribute("owner") String owner,
                             HttpServletRequest request,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute("message", "Autorized user is not defined!");
            return "error";
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute("message", "Link owner is not defined!");
            return "error";
        }
        if (shortLink == null || owner.equals("")) {
            model.addAttribute("message", "Old link is not defined!");
            return "error";
        }
        if (fullLink == null) {
            model.addAttribute("message", "Link is not defined!");
            return "error";
        }
        try {
            String contextPath = getContextPath(request);
            FullLink oldFullLink = service.getFullLink(
                    autorizedUser, shortLink, owner, contextPath);
            updateLink(autorizedUser, oldFullLink, fullLink);
            model.addAttribute("oldKey", null);
            return "redirect:links";
        } catch (RuntimeException e) {
            model.addAttribute("message", e.getMessage());
            return "error";
        }
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

    @RequestMapping(value = "/links", method = RequestMethod.GET)
    public String links(Model model,
                        @ModelAttribute("owner") String owner,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, links available only for logged users!");
            return "error";
        }

        if (request.getParameter("page") != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        if (owner == null || owner.equals("")) {
            owner = autorizedUser.getUserName();
        }
        String contextPath = getContextPath(request);
        int offset = (currentPage - 1) * recordsOnPage;
        List<FullLink> list = service.getFullStat(owner, contextPath, offset, recordsOnPage);
        long linksCount = (int) service.getUserLinksSize(autorizedUser, owner);
        if (linksCount == 0) {
            model.addAttribute("message", "Sorry, User don't have links. Try another!");
            return "error";
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) linksCount / recordsOnPage));

        model.addAttribute("list", list);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("owner", owner);

        return "links";
    }

    @RequestMapping(value = "/domains", method = RequestMethod.GET)
    public String domains(Model model,
                          HttpServletRequest request,
                          HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (request.getParameter("page") != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute("message", "Sorry, domains available only for admin users!");
            return PAGE_ERROR;
        }
        int offset = (currentPage - 1) * recordsOnPage;
        List<Domain> list = service.getShortStat(offset, recordsOnPage);
        long domainsSize = (int) service.getDomainsSize(autorizedUser);
        if (domainsSize == 0) {
            model.addAttribute("message", "Sorry, DB don't have domains visits. Try later!");
            return PAGE_ERROR;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) domainsSize / recordsOnPage));

        model.addAttribute("list", list);
        model.addAttribute("numberOfPages", numberOfPages);
        model.addAttribute("currentPage", currentPage);

        return "domains";
    }

    @RequestMapping(value = "/populate", method = RequestMethod.GET)
    public String populate(Model model,
                           HttpServletRequest request,
                           HttpSession session) {

        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute("message", "Sorry, populate available only for admin users!");
            return PAGE_ERROR;
        }

        String path = request.getServletContext().getRealPath(FILE_SEPARTOR);
        String context = getContextPath(request);

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext
                ("/spring/spring-app.xml");
        Thread populatorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Populator populator = applicationContext.getBean(Populator.class);
                populator.setPath(path);
                populator.setContext(context);
                populator.init();
            }
        });
        populatorThread.setName("Populator");
        populatorThread.start();
        return PAGE_MANAGE;
    }
    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public String chackExpired(Model model,
                           HttpServletRequest request,
                           HttpSession session) {

        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute("message", "Sorry, check expired links available only for admin " +
                    "users!");
            return PAGE_ERROR;
        }
        try {
            BigInteger expiredKeys=service.deleteExpiredUserLinks();
            System.out.println(expiredKeys.intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PAGE_MANAGE;
    }

    private void updateLink(User autorizedUser, FullLink oldFullLink, FullLink newFullLink) {
        String newKey = newFullLink.getKey();
        String oldKey = oldFullLink.getKey();
        StringBuilder message = new StringBuilder();
        if (oldKey == null || "".equals(oldKey)) {
            message.append("Updated link has empty key").append(System.lineSeparator());
        }
        if (newKey == null || "".equals(newKey)) {
            message.append("New link has empty key").append(System.lineSeparator());
        }
        if (oldFullLink.getUserName() == null || "".equals(oldFullLink.getUserName())) {
            message.append("Updated link has empty user").append(System.lineSeparator());
        }
        if (newFullLink.getUserName() == null || "".equals(newFullLink.getUserName())) {
            message.append("New link has empty user").append(System.lineSeparator());
        }
        if (!message.toString().equals("")) {
            throw new RuntimeException(message.toString());
        }
        service.updateLink(autorizedUser, oldFullLink, newFullLink);
    }
}
