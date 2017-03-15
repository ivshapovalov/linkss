package ru.ivan.linkss.controller;

import com.google.zxing.WriterException;
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
import ru.ivan.linkss.repository.entity.*;
import ru.ivan.linkss.service.LinksService;
import ru.ivan.linkss.util.Util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@EnableScheduling
@RequestMapping(value = "/manage/")
public class ManageController {

    private static final String FILE_SEPARTOR = File.separator;
    private static final String QR_FOLDER = "resources"+FILE_SEPARTOR+"qr";
    private static final String IMAGE_EXTENSION_WITH_DOT = ".png";

    private static final String WEB_SEPARTOR = "/";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_MESSAGE = "message";
    private static final String PAGE_MAIN = "main";
    private static final String PAGE_SIGNUP = "signup";
    private static final String PAGE_SIGNIN = "signin";
    private static final String PAGE_CONFIG = "config";
    private static final String PAGE_REGISTER = "register";
    private static final String PAGE_USER = "user";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_DOMAINS = "domains";
    private static final String PAGE_FREE_LINKS = "freelinks";
    private static final String PAGE_FREE_LINK = "freelink";
    private static final String PAGE_LINK = "link";
    private static final String PAGE_LINKS = "links";
    private static final String PAGE_VISITS = "visits";
    private static final String PAGE_VISIT = "visit";
    private static final String PAGE_MAP = "map";
    private static final String PAGE_ARCHIVE = "archive";
    private static final String PAGE_ARCHIVES = "archives";
    private static final String ACTION_LOGOUT = "logout";

    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_RESTORE = "restore";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_SAVE = "save";
    private static final String ACTION_POPULATE = "populate";
    private static final String ACTION_CHECK_EXPIRED = "checkExpired";

    private static final String ATTRIBUTE_USER = "user";
    private static final String ATTRIBUTE_LIST = "list";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ATTRIBUTE_POINTS = "points";
    private static final String ATTRIBUTE_IPS = "ips";
    private static final String ATTRIBUTE_TIME = "time";
    private static final String ATTRIBUTE_OLD_KEY = "oldKey";
    private static final String ATTRIBUTE_OLD_USERNAME = "oldUserName";
    private static final String ATTRIBUTE_OLD_PASSWORD = "oldPassword";
    private static final String ATTRIBUTE_FULL_LINK = "fullLink";
    private static final String ATTRIBUTE_OWNER = "owner";
    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_PAGE = "page";
    private static final String ATTRIBUTE_CURRENT_PAGE = "currentPage";
    private static final String ATTRIBUTE_NUMBER_OF_PAGES = "numberOfPages";
    private static final String ATTRIBUTE_VISITS_ACTUAL_SIZE = "visitsActualSize";
    private static final String ATTRIBUTE_VISITS_HISTORY_SIZE = "visitsHistorySize";

    @Autowired
    LinksService service;

    @RequestMapping(value = PAGE_SIGNUP, method = RequestMethod.GET)
    public String registration(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User.Builder().build());
        return PAGE_SIGNUP;
    }

    @RequestMapping(value = PAGE_SIGNIN, method = RequestMethod.GET)
    public String signin(Model model)
            throws IOException {
        model.addAttribute(ATTRIBUTE_USER, new User.Builder().build());
        return PAGE_SIGNIN;
    }

    @RequestMapping(value = ACTION_LOGOUT, method = RequestMethod.GET)
    public String logout(Model model, HttpSession session)
            throws IOException {
        model.addAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        session.setAttribute(ATTRIBUTE_AUTORIZED_USER, null);
        return "redirect:/";
    }

    @RequestMapping(value = PAGE_CONFIG, method = {RequestMethod.GET, RequestMethod.POST})
    public String config(Model model,HttpSession session)
            throws IOException {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser != null && !autorizedUser.isEmpty() && autorizedUser.isAdmin()) {
            model.addAttribute("linksSize", service.getDBLinksSize());
            model.addAttribute("freeLinksSize", service.getDBFreeLinksSize());
            model.addAttribute("usersSize", service.getUsersSize(autorizedUser));
            model.addAttribute("domainsSize", service.getDomainsSize(autorizedUser));
            return PAGE_CONFIG;
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
        List<UserDTO> usersDTO = service.getUsersDTO(offset, recordsOnPage);
        long usersCount = (int) service.getUsersSize(autorizedUser);
        if (usersCount == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, DB don't have users. Try later!");
            return PAGE_MESSAGE;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) usersCount / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, usersDTO);
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
            service.createUser(user);
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
                model.addAttribute(ATTRIBUTE_MESSAGE, "Login/password is incorrect");
                return PAGE_ERROR;
            }
        }
        return PAGE_SIGNIN;
    }

    private String autoLogin(Model model, User user, HttpSession session) {
        User dbUser= service.checkUser(user);
        if (dbUser!=null) {
            session.setAttribute(ATTRIBUTE_AUTORIZED_USER, dbUser);
           // model.addAttribute(ATTRIBUTE_AUTORIZED_USER, user);
            return "redirect:/";
        }
        return ACTION_LOGIN;
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK
            + WEB_SEPARTOR+"{"+ ATTRIBUTE_KEY + "}"+WEB_SEPARTOR+ ACTION_DELETE,method =
            RequestMethod.GET)
    public String deleteLink(Model model,
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

            return String.format("redirect:%s", getControllerMapping())
                    + PAGE_USER + WEB_SEPARTOR
                    + owner + WEB_SEPARTOR +PAGE_LINKS;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR + PAGE_ARCHIVE
            + WEB_SEPARTOR+"{"+ATTRIBUTE_KEY+"}"+WEB_SEPARTOR+ ACTION_DELETE,
            method = RequestMethod.GET)
    public String deleteArchiveLink(Model model,
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
            model.addAttribute(ATTRIBUTE_MESSAGE, "Archive link owner is not defined!");
            return PAGE_ERROR;
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Archive link is not defined!");
            return PAGE_ERROR;
        }

        String path = request.getServletContext().getRealPath("/");
        try {
            service.deleteArchiveLink(autorizedUser, shortLink, owner, path);
            model.addAttribute(ATTRIBUTE_KEY, null);
            model.addAttribute(ATTRIBUTE_OWNER, null);

            return String.format("redirect:%s", getControllerMapping())
                    + PAGE_USER + WEB_SEPARTOR
                    + owner + WEB_SEPARTOR +PAGE_ARCHIVES;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR + PAGE_ARCHIVE
            + WEB_SEPARTOR+ "{"+ATTRIBUTE_KEY+"}"+WEB_SEPARTOR+ACTION_RESTORE,
            method = RequestMethod.GET)
    public String restoreArchiveLink(Model model,
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
            model.addAttribute(ATTRIBUTE_MESSAGE, "Archive link owner is not defined!");
            return PAGE_ERROR;
        }
        if (shortLink == null || shortLink.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Archive link is not defined!");
            return PAGE_ERROR;
        }

        try {
            service.restoreArchiveLink(autorizedUser, shortLink, owner);
            model.addAttribute(ATTRIBUTE_KEY, null);
            model.addAttribute(ATTRIBUTE_OWNER, null);

            return String.format("redirect:%s", getControllerMapping())
                    + PAGE_USER + WEB_SEPARTOR
                    + owner + WEB_SEPARTOR +PAGE_ARCHIVES;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_FREE_LINK+ WEB_SEPARTOR
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

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK
            + WEB_SEPARTOR+"{"+ ATTRIBUTE_KEY + "}"+WEB_SEPARTOR
            +PAGE_VISIT+WEB_SEPARTOR+"{"+ATTRIBUTE_TIME+"}"+WEB_SEPARTOR+ACTION_DELETE,method =
            RequestMethod.GET)
    public String deleteVisit(Model model,
                             @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                              @ModelAttribute(ATTRIBUTE_KEY) String key,
                              @ModelAttribute(ATTRIBUTE_TIME) String time,
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
        if (key == null || key.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Link is not defined!");
            return PAGE_ERROR;
        }
        if (time == null || time.equals("")) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Time of visit is not defined!");
            return PAGE_ERROR;
        }

        try {
            service.deleteLinkVisit(autorizedUser, owner,key, time);
            model.addAttribute(ATTRIBUTE_OWNER, null);
            model.addAttribute(ATTRIBUTE_KEY, null);
            model.addAttribute(ATTRIBUTE_TIME, null);

            return String.format("redirect:%s", getControllerMapping())
                    + PAGE_USER + WEB_SEPARTOR
                    + owner + WEB_SEPARTOR +PAGE_LINK+WEB_SEPARTOR
                    +key +WEB_SEPARTOR+PAGE_VISITS;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = {WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK
            + WEB_SEPARTOR+"{"+ATTRIBUTE_KEY+"}"+WEB_SEPARTOR+ ACTION_EDIT}, method =
            RequestMethod.GET)
    public String editLink(Model model,
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
                    + QR_FOLDER + FILE_SEPARTOR + shortLink + IMAGE_EXTENSION_WITH_DOT;
            File imageOnDisk = new File(realImagePath);
            boolean created=true;
            if (!imageOnDisk.exists()) {
//                created=service.downloadImageFromFTP(shortLink+IMAGE_EXTENSION_WITH_DOT,
//                        realImagePath);
                created=false;
            }
            if (!created) {
                String context = getContextPath(request);
                String shortLinkPath = context + shortLink;
                try {
                    service.createQRImage(realImagePath,shortLink,shortLinkPath);
                    //service.uploadImageToFTP(realImagePath, shortLink);
                } catch (WriterException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String link = service.getLink(shortLink);
            String contextPath = getContextPath(request);
            String urlImagePath = contextPath + shortLink + IMAGE_EXTENSION_WITH_DOT;
            FullLink fullLink = new FullLink.Builder()
                    .addKey(shortLink)
                    .addShortLink(contextPath + shortLink)
                    .addLink(link)
                    .addImageLink(urlImagePath)
                    .addUserName(owner)
                    .addSeconds(service.getLinkExpirePeriod(shortLink))
                            .build();

            model.addAttribute(ATTRIBUTE_FULL_LINK, fullLink);
            model.addAttribute(ATTRIBUTE_OLD_KEY, shortLink);
            model.addAttribute(ATTRIBUTE_OWNER, owner);
            return PAGE_LINK;
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
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

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
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

    @RequestMapping(value = WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
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
            return String.format("redirect:%s%s",getControllerMapping(),PAGE_USERS);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +ACTION_SAVE}, method =
            RequestMethod.POST)
    public String updateUser(Model model,
                             @ModelAttribute(ATTRIBUTE_USER) User newUser,
                             @ModelAttribute(ATTRIBUTE_OLD_USERNAME) String oldUserName,
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
            User oldUser = new User.Builder().addUserName(oldUserName).build();
            newUser.setEmpty(false);
            service.updateUser(autorizedUser, newUser, oldUser);
            model.addAttribute("oldUserName", null);

            return String.format("redirect:%s%s",getControllerMapping(),PAGE_USERS);
        } catch (RuntimeException e) {
            model.addAttribute(ATTRIBUTE_MESSAGE, e.getMessage());
            return PAGE_ERROR;
        }
    }

    @RequestMapping(value = {WEB_SEPARTOR + ATTRIBUTE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK
            + WEB_SEPARTOR+"{"+ATTRIBUTE_OLD_KEY+"}"+WEB_SEPARTOR+ ACTION_SAVE}, method =
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

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINKS}, method = RequestMethod.GET)
    public String links(Model model,
                        @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, links available only for logged users!");
            return PAGE_MESSAGE;
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter(ATTRIBUTE_PAGE));
        }

        if (owner == null || owner.equals("")) {
            owner = autorizedUser.getUserName();
        }
        String contextPath = getContextPath(request);
        int offset = (currentPage - 1) * recordsOnPage;
        List<FullLink> list = service.getUserLinks(owner, contextPath, offset, recordsOnPage);
        long linksCount = (int) service.getUserLinksSize(autorizedUser, owner);
        if (linksCount == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, User don't have links. Try another!");
            return PAGE_MESSAGE;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) linksCount / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_OWNER, owner);

        return PAGE_LINKS;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK+WEB_SEPARTOR
            +"{"+ ATTRIBUTE_KEY + "}"+WEB_SEPARTOR+PAGE_VISITS}, method = RequestMethod.GET)
    public String visits(Model model,
                        @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                        @ModelAttribute(ATTRIBUTE_KEY) String key,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, visits available only for logged users!");
            return PAGE_MESSAGE;
        }

        if (!autorizedUser.isAdmin()) {
            if (!autorizedUser.getUserName().equals(owner)) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "watch user visits",
                        autorizedUser.getUserName()));
            }
        }

        if (request.getParameter(ATTRIBUTE_PAGE) != null) {
            currentPage = Integer.parseInt(request.getParameter(ATTRIBUTE_PAGE));
        }

        if (owner == null || owner.equals("")) {
            owner = autorizedUser.getUserName();
        }
        String contextPath = getContextPath(request);
        int offset = (currentPage - 1) * recordsOnPage;
        List<Visit> list = service.getLinkVisits(autorizedUser, owner,key, offset, recordsOnPage);
        long visitsCount = service.getLinkVisitsSize(autorizedUser, owner,key);
        if (visitsCount == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, User don't have visits on that link. " +
                    "Try another!");
            return PAGE_MESSAGE;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) visitsCount / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_OWNER, owner);
        model.addAttribute(ATTRIBUTE_KEY, key);

        return PAGE_VISITS;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_MAP}, method = RequestMethod.GET)
    public String mapUser(Model model,
                          @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                          HttpServletRequest request,
                          HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, visits available only for logged users!");
            return PAGE_MESSAGE;
        }
        if (!autorizedUser.isAdmin()) {
            if (!autorizedUser.getUserName().equals(owner)) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "watch user visits",
                        autorizedUser.getUserName()));
            }
        }

        //long visitsCount = service.getUserVisitsSize(autorizedUser, owner);
        List<Visit> visits=service.getUserVisits(autorizedUser,owner);
        visits = visits.stream()
                .filter(visit -> visit.getIpLocation()!=null)
                .filter(visit -> visit.getIpLocation().getLatitude()!=null && visit.getIpLocation()
                        .getLongitude()!=null)
                .filter(visit -> !"".equals(visit.getIpLocation().getLatitude()) & !"".equals(visit
                        .getIpLocation().getLongitude()))
                .collect(Collectors.toList());
        List<String> points=visits.stream()
                .map(visit -> "[" + String.valueOf(visit.getIpLocation().getLatitude()) + "," + String.valueOf(visit.getIpLocation().getLongitude() + "]"))
                .collect(Collectors.toList());
        List<String> ips=visits.stream()
                .map(visit -> "['" + String.valueOf(visit.getIpLocation().getIp())+"']")
                .collect(Collectors.toList());
        model.addAttribute(ATTRIBUTE_POINTS, points.toString());
        model.addAttribute(ATTRIBUTE_IPS, ips.toString());
        return PAGE_MAP;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR +PAGE_LINK+WEB_SEPARTOR
            +"{"+ ATTRIBUTE_KEY + "}"+WEB_SEPARTOR+PAGE_MAP}, method = RequestMethod.GET)
    public String mapLink(Model model,
                         @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                         @ModelAttribute(ATTRIBUTE_KEY) String key,
                         HttpServletRequest request,
                         HttpSession session) {
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, visits available only for logged users!");
            return PAGE_MESSAGE;
        }
        if (!autorizedUser.isAdmin()) {
            if (!autorizedUser.getUserName().equals(owner)) {
                throw new RuntimeException(String.format("User '%s' does not have permissions to " +
                                "watch user visits",
                        autorizedUser.getUserName()));
            }
        }

        long visitsCount = service.getLinkVisitsSize(autorizedUser, owner,key);
        List<Visit> visits=service.getLinkVisits(autorizedUser,owner,key,0,visitsCount);
        visits = visits.stream()
                .filter(visit -> visit.getIpLocation()!=null)
                .filter(visit -> visit.getIpLocation().getLatitude()!=null && visit.getIpLocation()
                        .getLongitude()!=null)
                .filter(visit -> !"".equals(visit.getIpLocation().getLatitude()) & !"".equals(visit
                        .getIpLocation().getLongitude()))
                .collect(Collectors.toList());
        List<String> points=visits.stream()
                .map(visit -> "[" + String.valueOf(visit.getIpLocation().getLatitude()) + "," + String.valueOf(visit.getIpLocation().getLongitude() + "]"))
                .collect(Collectors.toList());
        List<String> ips=visits.stream()
                .map(visit -> "['" + String.valueOf(visit.getIpLocation().getIp())+"']")
                .collect(Collectors.toList());
        model.addAttribute(ATTRIBUTE_POINTS, points.toString());
        model.addAttribute(ATTRIBUTE_IPS, ips.toString());
        return PAGE_MAP;
    }

    @RequestMapping(value = {WEB_SEPARTOR + PAGE_USER + WEB_SEPARTOR
            +"{"+ ATTRIBUTE_OWNER + "}"+WEB_SEPARTOR + PAGE_ARCHIVES}, method = RequestMethod.GET)
    public String archives(Model model,
                        @ModelAttribute(ATTRIBUTE_OWNER) String owner,
                        HttpServletRequest request,
                        HttpSession session) {
        int currentPage = 1;
        int recordsOnPage = 10;
        User autorizedUser = (User) session.getAttribute(ATTRIBUTE_AUTORIZED_USER);
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, archive available only for logged " +
                    "users!");
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
        List<FullLink> list = service.getUserArchive(owner, contextPath, offset, recordsOnPage);
        long archiveSize = (int) service.getUserArchiveSize(autorizedUser, owner);
        if (archiveSize == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, User don't have archive. Try another!");
            return PAGE_MESSAGE;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) archiveSize / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_OWNER, owner);

        return PAGE_ARCHIVES;
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
        long domainsSize = service.getDomainsSize(autorizedUser);
        long visitsActualSize = service.getVisitsActualSize(autorizedUser);
        long visitsHistorySize = service.getVisitsHistorySize(autorizedUser);
        if (domainsSize == 0) {
            model.addAttribute(ATTRIBUTE_MESSAGE, "Sorry, DB don't have domains visits. Try later!");
            return PAGE_MESSAGE;
        }
        int numberOfPages = Math.max(1, (int) Math.ceil((double) domainsSize / recordsOnPage));

        model.addAttribute(ATTRIBUTE_LIST, list);
        model.addAttribute(ATTRIBUTE_NUMBER_OF_PAGES, numberOfPages);
        model.addAttribute(ATTRIBUTE_CURRENT_PAGE, currentPage);
        model.addAttribute(ATTRIBUTE_VISITS_ACTUAL_SIZE, visitsActualSize);
        model.addAttribute(ATTRIBUTE_VISITS_HISTORY_SIZE, visitsHistorySize);

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
            return PAGE_MESSAGE;
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
                //service.clear(path);
                Populator populator = applicationContext.getBean(Populator.class);
                populator.setPath(path);
                populator.setContext(context);
                populator.init();
            }
        });
        populatorThread.setName("Populator");
        populatorThread.start();
        return "redirect:"+ PAGE_CONFIG;
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
        return PAGE_CONFIG;
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
