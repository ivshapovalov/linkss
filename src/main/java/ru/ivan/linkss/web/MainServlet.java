package ru.ivan.linkss.web;

import com.google.zxing.WriterException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.repository.FullLink;
import ru.ivan.linkss.service.Service;
import ru.ivan.linkss.service.ServiceImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

@Component
public class MainServlet extends HttpServlet {

    private Service service;

    private ConfigurableApplicationContext springContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        springContext = new ClassPathXmlApplicationContext("spring/spring-app.xml");
        service = springContext.getBean(ServiceImpl.class);
    }

    @Override
    public void destroy() {
        springContext.close();
        super.destroy();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //response.sendRedirect("index.jsp");
        String link = request.getParameter("link");
        if ("".equals(link)) {
            request.getRequestDispatcher("main.jsp").forward(request, response);
        }
        String shortLink = service.create(link);
        String path = getServletContext().getRealPath("/");
        try {
            service.createQRImage(path, link, shortLink);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        request.setAttribute("filename", shortLink+".png");
        request.setAttribute("link", link);
        request.setAttribute("shortLink", request.getRequestURL() + shortLink);
        //response.sendRedirect("main.jsp");
        request.getRequestDispatcher("main.jsp").forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //response.sendRedirect("index.jsp");
        String shortLink = request.getServletPath();
        if (shortLink.equals("/") || shortLink.equals("")) {
            request.setAttribute("filename", "");
            request.setAttribute("link", "");
            request.setAttribute("shortLink", "");

            request.getRequestDispatcher("main.jsp").forward(request, response);
        }
         else if (shortLink.equals("/stat")) {
            List<List<String>> shortStat=service.getShortStat();
            String p = request.getRequestURL().toString();
            String cp = request.getServletPath();

            String contextPath="";
            if (p.startsWith(cp)) {
                contextPath=p.substring(0,p.length()-cp.length()+1);
            }
            List<FullLink> fullStat=service.getFullStat(contextPath);
            request.setAttribute("shortStat", shortStat);
            request.setAttribute("fullStat", fullStat);
            request.getRequestDispatcher("stat.jsp").forward(request, response);

        } else if (shortLink.contains(".png") || shortLink.contains(".jpg")) {
            OutputStream os = response.getOutputStream();
            File file = new File(getServletContext().getRealPath(shortLink));
            FileInputStream fis = new FileInputStream(file);
            int bytes;
            while ((bytes = fis.read()) != -1) {
                os.write(bytes);
            }

        } else {
            String link = service.get(shortLink.substring(1));
            if (link.contains(":")) {
                response.sendRedirect(link);
            } else {
                response.sendRedirect("//" + link);
            }
        }
    }
}