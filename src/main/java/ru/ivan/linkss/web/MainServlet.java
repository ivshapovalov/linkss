package ru.ivan.linkss.web;

import com.google.zxing.WriterException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import ru.ivan.linkss.service.Service;
import ru.ivan.linkss.service.ServiceImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Component
public class MainServlet extends HttpServlet {

    private Service service;

    private ConfigurableApplicationContext springContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        springContext = new ClassPathXmlApplicationContext("spring/spring-app.xml");
        service = springContext.getBean(ServiceImpl.class);

//        super.init(config);
//        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
//                config.getServletContext());
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

        request.setAttribute("filename", shortLink);
        request.setAttribute("link", link);
//        request.setAttribute("shortLink", "https://linkss.herokuapp.com/" + shortLink);
        request.setAttribute("shortLink", "http://whydt.ru/" + shortLink);
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

        } else if (shortLink.contains(".png") || shortLink.contains(".jpg")) {
//            response.setContentType("application/png");
//            String path = getServletContext().getRealPath("/");
//            response.setHeader("Content-Disposition", "attachment; filename=\""+path+shortLink.substring(1)
//                    +"\"");

            InputStream is = null;
            OutputStream os = response.getOutputStream();
//            try {
//
//                response.setContentType("image/png");
//                response.setContentLength((int) b.length());
//                is = b.getBinaryStream();
//                byte buf[] = new byte[(int) b.length()];
//                is.read(buf);
//                os.write(buf);
//            } catch (Exception ex) {
            File file = new File(getServletContext().getRealPath(shortLink));
            FileInputStream fis = new FileInputStream(file);
            int bytes;
            while ((bytes = fis.read()) != -1) {
                os.write(bytes);
            }
            //}

        } else {
            String link = service.get(shortLink.substring(1));
            if (link.contains(":")) {
                response.sendRedirect(link);
            } else {
                response.sendRedirect("//" + link);
            }
            //response.sendRedirect(link);

        }
    }


}