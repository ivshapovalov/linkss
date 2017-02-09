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
import ru.ivan.linkss.util.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Controller
@EnableScheduling
@RequestMapping(value = "/actions/")
public class ActionsController {

    private static final String ACTION_EDIT = "edit";
    private static final String FILE_SEPARTOR = File.separator;
    private static final String RESOURCE_FOLDER = "resources";
    private static final String IMAGE_EXTENSION = ".png";

    private static final String WEB_SEPARTOR = "/";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_MAIN = "main";
    private static final String PAGE_SIGNUP = "signup";
    private static final String PAGE_SIGNIN = "signin";
    private static final String PAGE_MANAGE = "manage";
    private static final String PAGE_REGISTER = "register";
    private static final String PAGE_USER = "user";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_DOMAINS = "domains";
    private static final String PAGE_FREE_LINKS = "freelinks";
    private static final String PAGE_LINK = "link";
    private static final String PAGE_LINKS = "links";
    private static final String ACTION_LOGOUT = "logout";

    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_POPULATE = "populate";
    private static final String ACTION_CHECK_EXPIRED = "checkExpired";

    private static final String ATTRIBUTE_USER = "user";
    private static final String ATTRIBUTE_LINKS = "links";
    private static final String ATTRIBUTE_LIST = "list";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_OLD_KEY = "oldKey";
    private static final String ATTRIBUTE_OLD_USERNAME = "oldUserName";
    private static final String ATTRIBUTE_OLD_PASSWORD = "oldPassword";
    private static final String ATTRIBUTE_FULL_LINK = "fullLink";
    private static final String ATTRIBUTE_OWNER = "owner";
    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_PAGE = "page";
    private static final String ATTRIBUTE_CURRENT_PAGE = "currentPage";
    private static final String ATTRIBUTE_NUMBER_OF_PAGES = "numberOfPages";

    @Autowired
    private LinksService service;

    @RequestMapping(value = PAGE_SIGNUP, method = RequestMethod.GET)
    public String registration(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User());
        return PAGE_SIGNUP;
    }

    @RequestMapping(value = PAGE_SIGNIN, method = RequestMethod.GET)
    public String signin(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User());
        return PAGE_SIGNIN;
    }

    @RequestMapping(value = ACTION_LOGOUT, method = RequestMethod.GET)
    public String logout(Model model, HttpSession session)
            throws IOException {
        model.addAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        session.setAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        return "redirect:/";
    }

    @RequestMapping(value = PAGE_MANAGE, method = {RequestMethod.GET, RequestMethod.POST})
    public String manage(Model model,HttpSession session)
            throws IOException {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && !autorizedUser.isEmpty() && autorizedUser.isAdmin()) {
            model.addAttribute("linksSize", service.getDBLinksSize());
            model.addAttribute("linksSize", service.getDBLinksSize());
            model.addAttribute("freeLinksSize", service.getDBFreeLinksSize());
            model.addAttribute("usersSize", service.getUsersSize(autorizedUser));
            model.addAttribute("domainsSize", service.getDomainsSize(autorizedUser));
            return PAGE_MANAGE;
        }
        return PAGE_MAIN;
    }

    @RequestMapping(value = PAGE_USERS, method = {RequestMethod.GET, RequestMethod.POST})
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

        model.addAttribute(ATTRIBUTE_LIST, users);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_AUTORIZED_USER, autorizedUser);
        return PAGE_USERS;
    }

    @RequestMapping(value = PAGE_REGISTER, method = RequestMethod.POST)
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

    @RequestMapping(value = ACTION_LOGIN, method = RequestMethod.POST)
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
        return PAGE_SIGNIN;
    }

    private String autoLogin(Model model, User user, HttpSession session) {
        boolean existedUser = service.checkUser(user);
        if (existedUser) {
            user.setEmpty(false);
            //TODO get user in json
            if ("admin".equals(user.getUserName())) {
                user.setAdmin(true);
            }
            session.setAttribute(ATTRIBUTE_AUTORIZED_USER, user);
            model.addAttribute(ATTRIBUTE_AUTORIZED_USER, user);
            return "redirect:/";
        }
        return ACTION_LOGIN;
    }

    @RequestMapping(value = WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ATTRIBUTE_LINKS
            + WEB_SEPARTOR+ ACTION_DELETE,
            method = RequestMethod.GET)
    public String deleteUserlink(Model model,
                                 @ModelAttribute(ATTRIBUTE_KEY) String shortLink,
                                 @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User is not defined!");
            return PAGE_ERROR;
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link owner is not defined!");
            return PAGE_ERROR;
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link is not defined!");
            return PAGE_ERROR;
        }

        try {
            service.deleteUserLink(autorizedUser, shortLink, owner);
            model.addAttribute(ATTRIBUTE_KEY, null);
            model.addAttribute(ATTRIBUTE_OWNER, null);

            return String.format("redirect:%s%s?%s=%s", getControllerMapping(), ATTRIBUTE_LINKS,
                    ATTRIBUTE_OWNER, owner);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_FREE_LINKS+ WEB_SEPARTOR
            +"{"+ ATTRIBUTE_KEY + "}"+WEB_SEPARTOR + ACTION_DELETE,
            method = RequestMethod.GET)
    public String deleteFreeLink(Model model,
                                 @ModelAttribute(ATTRIBUTE_KEY) String shortLink,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User is not defined!");
            return PAGE_ERROR;
        }

        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link is not defined!");
            return PAGE_ERROR;
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, free links deleting available only for " +
                    "admin users!");
            return PAGE_ERROR;
        }

        try {
            service.deleteFreeLink(shortLink);
            model.addAttribute(ATTRIBUTE_KEY, null);
            return String.format("redirect:%s%s", getControllerMapping(), PAGE_FREE_LINKS);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    private String getControllerMapping() {
        try {
            String[] path=this.getClass().getAnnotation(RequestMapping.class).value();
            if (path!=null && path.length==1) {
                return path[0];
            }
        } catch (Exception e) {

        }
        return "";
    }

    @RequestMapping(value = WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ATTRIBUTE_LINKS
            + WEB_SEPARTOR+ ACTION_EDIT, method = RequestMethod.GET)
    public String updateuserlink(Model model,
                                 @ModelAttribute(ATTRIBUTE_KEY) String shortLink,
                                 @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User is not defined!");
            return PAGE_ERROR;
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link owner is not defined!");
            return PAGE_ERROR;
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link is not defined!");
            return PAGE_ERROR;
        }

        try {
            String realImagePath = request.getServletContext().getRealPath("")
                    + RESOURCE_FOLDER + FILE_SEPARTOR + shortLink + IMAGE_EXTENSION;
            File imageOnDisk = new File(realImagePath);
            if (!imageOnDisk.exists()) {
                Util.downloadImageFromS3(realImagePath, shortLink);
            }

            String link = service.getLink(shortLink);
            String contextPath = getContextPath(request);
            String urlImagePath = contextPath + shortLink + IMAGE_EXTENSION;
            FullLink fullLink = new FullLink(shortLink, contextPath + shortLink, link,
                    "", urlImagePath,
                    owner, service.getLinkExpirePeriod(shortLink));
            model.addAttribute(ATTRIBUTE_FULL_LINK, fullLink);
            model.addAttribute(ATTRIBUTE_OLD_KEY, shortLink);
            model.addAttribute(ATTRIBUTE_OWNER, owner);
            return PAGE_LINK;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USERS + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ACTION_CLEAR, method = RequestMethod.GET)
    public String clearUser(Model model,
                            @PathVariable(ATTRIBUTE_OWNER) String owner,
                            HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);

        if (autorizedUser == null || "".equals(autorizedUser.getUserName())) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Autorized user is not defined!");
            return PAGE_ERROR;
        }

        if (owner == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User name is not defined!");
            return PAGE_ERROR;
        }
        try {
            service.clearUser(autorizedUser, owner);
            model.addAttribute(ATTRIBUTE_OWNER, null);
            return String.format("redirect:%s%s",getControllerMapping(),PAGE_USERS);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USERS + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ACTION_EDIT, method = RequestMethod.GET)
    private String editUser(Model model, @PathVariable(ATTRIBUTE_OWNER) String key
            , HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);

        if (autorizedUser == null || autorizedUser.getUserName() == null || "".equals(autorizedUser.getUserName())) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Autorized user is not defined!");
            return PAGE_ERROR;
        }
        if (key == null || key.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User name is not defined!");
            return PAGE_ERROR;
        }
        try {
            User user = service.getUser(autorizedUser, key);
            model.addAttribute(ATTRIBUTE_USER, user);
            model.addAttribute(ATTRIBUTE_OLD_USERNAME, user.getUserName());
            model.addAttribute(ATTRIBUTE_OLD_PASSWORD, user.getPassword());
            return PAGE_USER;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USERS + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ACTION_DELETE, method = RequestMethod.GET)
    private String deleteUser(Model model, @PathVariable(ATTRIBUTE_OWNER) String owner, HttpSession
            session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);

        if (autorizedUser == null || "".equals(autorizedUser.getUserName())) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Autorized user is not defined!");
            return PAGE_ERROR;
        }

        if (owner == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User name is not defined!");
            return PAGE_ERROR;
        }
        try {
            service.deleteUser(autorizedUser, owner);
            model.addAttribute(ATTRIBUTE_OWNER, null);
            model.addAttribute(ATTRIBUTE_ACTION, null);
            return String.format("redirect:%s%s",getControllerMapping(),PAGE_USERS);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = {WEB_SEPARTOR+PAGE_USER}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute(ATTRIBUTE_USER) User newUser,
                             @ModelAttribute(ATTRIBUTE_OLD_USERNAME) String oldUserName,
                             @ModelAttribute(ATTRIBUTE_OLD_PASSWORD) String oldPassword,
                             HttpServletRequest request,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Autorized user is not defined!");
            return PAGE_ERROR;
        }
        if (newUser == null) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "User for update is not defined!");
            return PAGE_ERROR;
        }
        try {
            User oldUser = new User(oldUserName, oldPassword);
            service.updateUser(autorizedUser, newUser, oldUser);
            model.addAttribute("oldUserName", null);
            model.addAttribute("oldPassword", null);

            return String.format("redirect:%s%s",getControllerMapping(),PAGE_MANAGE);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = PAGE_LINK, method =
            RequestMethod.POST)
    public String updateLink(Model model,
                             @ModelAttribute(ATTRIBUTE_FULL_LINK) FullLink fullLink,
                             @ModelAttribute(ATTRIBUTE_OLD_KEY) String shortLink,
                             @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                             @ModelAttribute("secondsText") String secondsText,
                             HttpServletRequest request,
                             HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.getUserName() == null || autorizedUser.getUserName().equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Autorized user is not defined!");
            return PAGE_ERROR;
        }
        if (owner == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link owner is not defined!");
            return PAGE_ERROR;
        }
        if (shortLink == null || owner.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Old link is not defined!");
            return PAGE_ERROR;
        }
        if (fullLink == null) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link is not defined!");
            return PAGE_ERROR;
        }
        try {
            long seconds=Util.convertPeriodToSeconds(secondsText);
            fullLink.setSeconds(seconds);
            String contextPath = getContextPath(request);
            FullLink oldFullLink = service.getFullLink(
                    autorizedUser, shortLink, owner, contextPath);
            updateLink(autorizedUser, oldFullLink, fullLink);
            model.addAttribute(ATTRIBUTE_OLD_KEY, null);
            return "redirect:" + PAGE_LINKS;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
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

    @RequestMapping(value = PAGE_LINKS, method = RequestMethod.GET)
    public String links(Model model,
                        @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter(ATTRIBUTE_PAGE));
        }

        if (owner == null || owner.equals("")) {
            owner = autorizedUser.getUserName();
        }
        String contextPath = getContextPath(request);
        int offset = (currentPage - 1) * recordsOnPage;
        List<FullLink> list = service.getFullStat(owner, contextPath, offset, recordsOnPage);
        long linksCount = (int) service.getUserLinksSize(autorizedUser, owner);
        if (linksCount == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, User don't have links. Try another!");
            return PAGE_ERROR;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) linksCount / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_OWNER, owner);

        return PAGE_LINKS;
    }

    @RequestMapping(value = PAGE_DOMAINS, method = RequestMethod.GET)
    public String domains(Model model,
                          HttpServletRequest request,
                          HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, domains available only for admin users!");
            return PAGE_ERROR;
        }
        int offset = (currentPage - 1) * recordsOnPage;
        List<Domain> list = service.getShortStat(offset, recordsOnPage);
        long domainsSize = (int) service.getDomainsSize(autorizedUser);
        if (domainsSize == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, DB don't have domains visits. Try later!");
            return PAGE_ERROR;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) domainsSize / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);

        return PAGE_DOMAINS;
    }

    @RequestMapping(value = PAGE_FREE_LINKS, method = RequestMethod.GET)
    public String freeLinks(Model model,
                          HttpServletRequest request,
                          HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, free links available only for logged " +
                    "users!");
            return PAGE_ERROR;
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter("page"));
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, free links available only for admin " +
                    "users!");
            return PAGE_ERROR;
        }
        int offset = (currentPage - 1) * recordsOnPage;
        List<String> list = service.getFreeLinks(offset, recordsOnPage);
        long freeLinksSize = (int) service.getDBFreeLinksSize();
        if (freeLinksSize == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, DB don't have free links. Try " +
                    "later!");
            return PAGE_ERROR;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) freeLinksSize / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);

        return PAGE_FREE_LINKS;
    }

    @RequestMapping(value = ACTION_POPULATE, method = RequestMethod.GET)
    public String populate(Model model,
                           HttpServletRequest request,
                           HttpSession session) {

        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, populate available only for admin users!");
            return PAGE_ERROR;
        }

        String path = request.getServletContext().getRealPath(WEB_SEPARTOR);
        String context = getContextPath(request);

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext
                ("/spring/spring-mvc.xml");
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

    @RequestMapping(value = ACTION_CHECK_EXPIRED, method = RequestMethod.GET)
    public String checkExpired(Model model,
                               HttpServletRequest request,
                               HttpSession session) {

        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, links available only for logged users!");
            return PAGE_ERROR;
        }

        if (!autorizedUser.isAdmin()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, check expired links available only for admin " +
                    "users!");
            return PAGE_ERROR;
        }
        try {
            BigInteger expiredKeys = service.deleteExpiredUserLinks();
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
