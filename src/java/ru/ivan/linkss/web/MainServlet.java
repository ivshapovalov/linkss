package ru.ivan.linkss.web;

import ru.ivan.linkss.service.Service;
import ru.ivan.linkss.service.ServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MainServlet extends HttpServlet {

    Service service = new ServiceImpl();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //response.sendRedirect("index.jsp");
        String link = request.getParameter("link");
        String shortLink = service.create(link);
        request.setAttribute("shortLink", "http://linkss/" + shortLink);
        request.getRequestDispatcher("main.jsp").forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //response.sendRedirect("index.jsp");
        String shortLink = request.getServletPath();
        if (shortLink.equals("/")) {
            request.getRequestDispatcher("main.jsp").forward(request, response);
        } else {

            String link=service.get(shortLink.substring(1));
            response.sendRedirect("//"+link);
        }
    }


}