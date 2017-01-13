package ru.ivan.linkss.controller;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.ivan.linkss.repository.FullLink;
import ru.ivan.linkss.service.LinkssService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private LinkssService service;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main() {
        return "main";
    }

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String redirect(HttpServletRequest request) {
        String shortLink = request.getServletPath();

        String link = service.get(shortLink.substring(1));
        if (link.contains(":")) {
            return "redirect:"+link;
        } else {
            return "redirect:"+"//"+link;
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

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        return "registration";

    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String createShortLink(Model model, HttpServletRequest request) {
        String link = request.getParameter("link");
        if ("".equals(link)) {
            return "main";
        }
        String shortLink = service.create(link);
        if (shortLink==null) {
            model.addAttribute("message", "Sorry, service is unavailable! Try later");
            return "error";
        }
        String path = request.getServletContext().getRealPath("/");
        try {
            service.createQRImage(path, shortLink, request.getRequestURL() + shortLink);

        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }

        model.addAttribute("filename", shortLink + ".png");
        model.addAttribute("link", link);
        model.addAttribute("shortLink", request.getRequestURL() + shortLink);
        return "main";
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public String statistics(Model model, HttpServletRequest request) {
        List<List<String>> shortStat = service.getShortStat();
        String p = request.getRequestURL().toString();
        String cp = request.getServletPath();

        String contextPath = "";
        if (p.startsWith(cp)) {
            contextPath = p.substring(0, p.length() - cp.length() + 1);
        }
        List<FullLink> fullStat = service.getFullStat(contextPath);
        model.addAttribute("shortStat", shortStat);
        model.addAttribute("fullStat", fullStat);
        return "statistics";
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
//            Object ob = tableData.get(column);
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
//                    Object parameter = allRequestParams.get(columnName);
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
//                    Object parameter = allRequestParams.get(columnName);
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
//                    columnParameters.put((String) allRequestParams.get("columnName" + index),
//                            allRequestParams.get("columnType" + index));
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
