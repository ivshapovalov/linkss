package ru.ivan.linkss.controller;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
            //AuthorizedUser.valueOf(autorizedUser);
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

    @RequestMapping(value = "/actions/registration", method = RequestMethod.GET)
    public String registration(Model model, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        model.addAttribute("user", new User());

        return "registration";

    }

    @RequestMapping(value = "/actions/signin", method = RequestMethod.GET)
    public String signin(Model model)
            throws IOException {
        model.addAttribute("user", new User());

        return "signin";

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
                           HttpSession session,
                           final BindingResult binding) {
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
        return "signin";

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

    @RequestMapping(value = {"/actions/deleteuserlink"}, method =
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
            String p = request.getRequestURL().toString();
            String cp = request.getServletPath();

            String contextPath = "";
            if (p.endsWith(cp)) {
                contextPath = p.substring(0, p.length() - cp.length() + 1);
            }
            List<FullLink> fullStat = service.getFullStat(contextPath);
            model.addAttribute("shortStat", shortStat);
            model.addAttribute("fullStat", fullStat);
        } else {
            String p = request.getRequestURL().toString();
            String cp = request.getServletPath();

            String contextPath = "";
            if (p.endsWith(cp)) {
                contextPath = p.substring(0, p.length() - cp.length() + 1);
            }
            List<FullLink> fullStat = service.getFullStat(autorizedUser.getUserName(), contextPath);
            model.addAttribute("fullStat", fullStat);
        }
        return "statistics";
    }

    @RequestMapping(value = "/actions/links", method = RequestMethod.GET)
    public String links(Model model,
                        @ModelAttribute("owner") String owner,
                        HttpServletRequest request,
                        HttpSession session) {

        User autorizedUser = (User) session.getAttribute("autorizedUser");
        if (autorizedUser == null || autorizedUser.isEmpty()) {
            model.addAttribute("message", "Sorry, statistics available only for logged users!");
            return "error";
        }
        String p = request.getRequestURL().toString();
        String cp = request.getServletPath();

        String contextPath = "";
        if (p.endsWith(cp)) {
            contextPath = p.substring(0, p.length() - cp.length() + 1);
        }
        List<FullLink> fullStat = service.getFullStat(owner, contextPath);
        model.addAttribute("fullStat", fullStat);

        return "links";
    }

//    @RequestMapping(value = "/databases", method = RequestMethod.GET)
//    public String databases(Model model, HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/databases");
//            return "redirect:/connect";
//        }
//
//        String currentDatabase = (String) session.getAttribute("db_name");
//        if (currentDatabase != null) {
//                model.addAttribute("currentDatabase", currentDatabase);
//         }
//
//        model.addAttribute("databases", service.databases(manager));
//        return "databases";
//    }
//
//    @RequestMapping(value = "/connect", method = RequestMethod.GET)
//    public String connect(Model model, @ModelAttribute("database")
//            String database, HttpSession session) {
//        String page = (String) session.getAttribute("from-page");
//        session.removeAttribute("from-page");
//        if (database != null && !database.equals("")) {
//            model.addAttribute("connection", new Connection(database, page));
//            return "connect";
//        } else {
//            model.addAttribute("connection", new Connection(page));
//            if (getManager(session) == null) {
//                return "connect";
//            } else {
//                session.setAttribute("from-page", "/menu");
//                return menu(model);
//            }
//        }
//    }
//
//    @RequestMapping(value = "/connect", method = RequestMethod.POST)
//    public String connecting(@ModelAttribute("connection") Connection connection,
//                             HttpSession session, Model model) {
//        try {
//            DatabaseManager manager = service.connect(connection.getDbName(),
//                    connection.getUserName(), connection.getPassword());
//            session.setAttribute("manager", manager);
//            session.setAttribute("db_name", connection.getDbName());
//            return "redirect:" + connection.getFromPage();
//        } catch (Exception e) {
//            e.printStackTrace();
//            model.addAttribute("message", e.getMessage());
//            return "error";
//        }
//    }
//
//    @RequestMapping(value = "/rows", method = RequestMethod.GET)
//    public String rows(Model model,
//                       @ModelAttribute("table") String tableName,
//                       HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows/" + tableName);
//            return "redirect:/connect";
//        }
//        model.addAttribute("table", service.rows(manager, tableName));
//        model.addAttribute("tableName", tableName);
//        return "rows";
//    }
//
//    @RequestMapping(value = "/opendatabase", method = RequestMethod.GET)
//    public String openDatabase(Model model,
//                               @ModelAttribute("database") String database,
//                               HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/databases");
//            return "redirect:/connect";
//        }
//        model.addAttribute("databaseName", database);
//        String currentDatabase = (String) session.getAttribute("db_name");
//        if (currentDatabase != null) {
//            if (currentDatabase.equals(database)) {
//                model.addAttribute("currentDatabase", true);
//            }
//        }
//        return "opendatabase";
//    }
//
//
//    @RequestMapping(value = "/createdatabase", method = {RequestMethod.GET})
//    public String createDatabase() {
//        return "createdatabase";
//
//    }
//
//    @RequestMapping(value = "/createdatabase", method = {RequestMethod.POST})
//    public String createDatabase(Model model,
//                                 @ModelAttribute("database") String database,
//                                 HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/databases");
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.createDatabase(database);
//                model.addAttribute("message", "New database created successfully!");
//                model.addAttribute("link", "databases");
//                model.addAttribute("title", "Back to databases list");
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", "Incorrect database name. Try again!");
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/dropdatabase", method = {RequestMethod.POST, RequestMethod.GET})
//    public String dropDatabase(Model model,
//                               @ModelAttribute("database") String database,
//                               HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/databases");
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.dropDatabase(database);
//                model.addAttribute("message", String.format("Database '%s' dropped successfully!",
//                        database));
//                model.addAttribute("link", "databases");
//                model.addAttribute("title", "Back to databases list");
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Database '%s' cannot be dropped!",
//                        database));
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/truncatedatabase", method = {RequestMethod.GET})
//    public String truncateDatabase(Model model,
//                                   @ModelAttribute("database") String database,
//                                   HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/opendatabase?database="+database);
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.truncateAllTables();;
//                model.addAttribute("message", String.format("Database '%s' truncated successfully!",
//                        database));
//                model.addAttribute("link", "databases");
//                model.addAttribute("title", "Back to databases list");
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Database '%s' cannot be truncated!",
//                        database));
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/droptable", method = {RequestMethod.GET})
//    public String dropTable(Model model,
//                            @ModelAttribute("table") String tableName,
//                            HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/tables");
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.dropTable(tableName);
//                model.addAttribute("message", String.format("Table '%s' dropped successfully!",
//                        tableName));
//                model.addAttribute("link", "tables");
//                model.addAttribute("title", "Back to tables list");
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Table '%s' cannot be dropped!",
//                        tableName));
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/truncatetable", method = {RequestMethod.GET})
//    public String truncateTable(Model model,
//                                @ModelAttribute("table") String tableName,
//                                HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/tables");
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.truncateTable(tableName);
//                model.addAttribute("message", String.format("Table '%s' truncated successfully!",
//                        tableName));
//                model.addAttribute("link", "tables");
//                model.addAttribute("title", "Back to tables list");
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Table '%s' cannot be truncated!",
//                        tableName));
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/openrow", method = {RequestMethod.GET})
//    public String openRow(Model model,
//                          @ModelAttribute("table") String tableName,
//                          @ModelAttribute("id") int id,
//                          HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows?table=" + tableName);
//            return "redirect:/connect";
//        } else {
//            model.addAttribute("tableName", tableName);
//            model.addAttribute("id", id);
//            model.addAttribute("table", getRow(manager, tableName, id));
//            return "openrow";
//        }
//    }
//
//    public List<List<String>> getRow(final DatabaseManager manager, final String tableName, final int id) {
//        List<List<String>> result = new LinkedList<>();
//        List<String> columns = new LinkedList<>(manager.getTableColumns(tableName));
//        Map<String, Object> tableData = manager.getRow(tableName, id);
//
//        for (String column : columns) {
//            List<String> row = new ArrayList<>(2);
//            row.add(column);
//            Object ob = tableData.getLink(column);
//            if (ob != null) {
//                row.add(ob.toString());
//            } else {
//                row.add("");
//
//            }
//            result.add(row);
//        }
//        return result;
//    }
//
//    @RequestMapping(value = "/deleterow", method = {RequestMethod.GET})
//    public String deleteRow(Model model,
//                            @ModelAttribute("table") String tableName,
//                            @ModelAttribute("id") int id,
//                            HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows?table=" + tableName);
//            return "redirect:/connect";
//        } else {
//            try {
//                manager.deleteRow(tableName, id);
//                model.addAttribute("message", String.format("Row with id='%s' in table='%s' " +
//                                "deleted successfully!", id,
//                        tableName));
//                model.addAttribute("link", "rows?table=" + tableName);
//                model.addAttribute("title", String.format("Back to tables '%s' rows ", tableName));
//                return "message";
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Row with id='%s' in table='%s' cannot be deleted!", id,
//                        tableName));
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/updaterow", method = {RequestMethod.POST})
//    public String updateRow(Model model,
//                            @ModelAttribute("tableName") String tableName,
//                            @ModelAttribute("id") int id,
//                            @RequestParam Map<String, Object> allRequestParams,
//                            HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/openrow?table=" + tableName + "&id=" + id);
//            return "redirect:/connect";
//        } else {
//            try {
//                List<String> columnNames = new LinkedList<>(manager.getTableColumns(tableName));
//                Map<String, Object> row = new LinkedHashMap<>();
//                for (String columnName : columnNames
//                        ) {
//                    Object parameter = allRequestParams.getLink(columnName);
//                    row.put(columnName, parameter);
//                }
//                row.remove("id");
//
//                manager.updateRow(tableName, "id", String.valueOf(id), row);
//                model.addAttribute("message", String.format("Row with id='%s' updated " +
//                        "successfully!", id));
//                model.addAttribute("link", "rows?table=" + tableName);
//                model.addAttribute("title", String.format("Back to table '%s' rows ",
//                        tableName));
//                return "message";
//
//            } catch (Exception e) {
//                model.addAttribute("message", "Incorrect data. Try again!");
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/insertrow", method = {RequestMethod.GET})
//    public String insertRow(Model model,
//                            @ModelAttribute("table") String tableName,
//                            HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows?table=" + tableName);
//            return "redirect:/connect";
//        } else {
//            model.addAttribute("tableName", tableName);
//            model.addAttribute("columns", new LinkedList<>(manager.getTableColumnsWithType
//                    (tableName)));
//            return "insertrow";
//        }
//    }
//
//    @RequestMapping(value = "/insertrow", method = {RequestMethod.POST})
//    public String insertRow(Model model,
//                            @ModelAttribute("table") String tableName,
//                            @ModelAttribute("id") int id,
//                            @RequestParam Map<String, Object> allRequestParams,
//                            HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows?table=" + tableName);
//            return "redirect:/connect";
//        } else {
//            try {
//                List<String> columnNames = new LinkedList<>(manager.getTableColumns(tableName));
//                Map<String, Object> row = new LinkedHashMap<>();
//                for (String columnName : columnNames
//                        ) {
//                    Object parameter = allRequestParams.getLink(columnName);
//                    row.put(columnName, parameter);
//                }
//                manager.insertRow(tableName, row);
//                model.addAttribute("message", "New row inserted successfully!");
//                model.addAttribute("link", "rows?table=" + tableName);
//                model.addAttribute("title", String.format("Back to table '%s' rows ",
//                        tableName));
//                return "message";
//
//            } catch (Exception e) {
//                model.addAttribute("message", "Incorrect data. Try again!");
//                return "error";
//            }
//        }
//    }
//
//    @RequestMapping(value = "/createtable", method = {RequestMethod.GET})
//    public String createTable() {
//        return "createtable";
//    }
//
//    @RequestMapping(value = "/newtable", method = {RequestMethod.GET})
//    public String newTable(Model model,
//                           @ModelAttribute("tableName") String tableName,
//                           @ModelAttribute("columnCount")
//                                   int columnCount,
//                           HttpSession session) {
//
//        if (columnCount < 1) {
//            model.addAttribute("message", String.format("Column count must be greater than 1, but" +
//                            " actual %s",
//                    columnCount));
//            return "error";
//        }
//
//        model.addAttribute("tableName", tableName);
//        model.addAttribute("columnCount", columnCount);
//        return "newtable";
//    }
//
//    @RequestMapping(value = "/newtable", method = {RequestMethod.POST})
//    public String newTable(Model model,
//                           @ModelAttribute("tableName") String tableName,
//                           @ModelAttribute("columnCount") int columnCount,
//                           @ModelAttribute("keyName") String keyName,
//                           @RequestParam Map<String, Object> allRequestParams,
//                           HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/rows?table=" + tableName);
//            return "redirect:/connect";
//        } else {
//            try {
//                Map<String, Object> columnParameters = new LinkedHashMap<>();
//                for (int index = 1; index < columnCount; index++) {
//                    columnParameters.put((String) allRequestParams.getLink("columnName" + index),
//                            allRequestParams.getLink("columnType" + index));
//                }
//                String query = tableName + "(" + keyName + " INT PRIMARY KEY NOT NULL"
//                        + getParameters(columnParameters) + ")";
//                manager.createTable(query);
//                model.addAttribute("message", String.format("Table '%s' created successfully!",
//                        tableName));
//                model.addAttribute("link", "tables");
//                model.addAttribute("title", "Back to tables list");
//                return "message";
//
//            } catch (Exception e) {
//                model.addAttribute("message", String.format("Table '%s' not created. Try again!",
//                        tableName));
//                return "error";
//            }
//        }
//    }
//
//    private String getParameters(final Map<String, Object> columnParameters) {
//        String result = "";
//        for (Map.Entry<String, Object> pair : columnParameters.entrySet()) {
//            result += ", " + pair.getKey() + " " + pair.getValue();
//        }
//        return result;
//    }
//
//    @RequestMapping(value = "/menu", method = RequestMethod.GET)
//    public String menu(Model model) {
//        model.addAttribute("items", service.getMainMenu());
//        return "menu";
//    }
//
//    @RequestMapping(value = "/tables", method = RequestMethod.GET)
//    public String tables(Model model, HttpSession session) {
//        DatabaseManager manager = getManager(session);
//
//        if (manager == null) {
//            session.setAttribute("from-page", "/tables");
//            return "redirect:/connect";
//        }
//
//        model.addAttribute("tables", service.tables(manager));
//        return "tables";
//    }
//
//    private DatabaseManager getManager(HttpSession session) {
//        return (DatabaseManager) session.getAttribute("manager");
//    }

}
