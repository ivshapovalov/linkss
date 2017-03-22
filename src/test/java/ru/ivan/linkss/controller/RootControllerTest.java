package ru.ivan.linkss.controller;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class RootControllerTest {

    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String ATTRIBUTE_LINK = "link";

    private static final String PAGE_MAIN = "main";
    private static final String PAGE_MESSAGE = "message";
    private static final String PAGE_ERROR = "error";

    private static final String IMAGE_EXTENSION = ".png";
    private static final String ICON_EXTENSION = ".ico";


    RootController controller;

    @Before
    public void setUp() {
        controller = new RootController();
    }

    @Test
    public void mainTestOk() {
        //given
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        User user = new User.Builder()
                .addUserName("admin")
                .addIsAdmin(true)
                .addIsVerified(true)
                .build();
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);

        //when
        String actual = controller.main(model, session);

        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(model).addAttribute(ATTRIBUTE_AUTORIZED_USER, user);
        Mockito.verifyNoMoreInteractions(session, model);
        assertEquals(PAGE_MAIN, actual);
    }

    @Test
    public void mainTestWithoutUser() {
        //given
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(null);

        //when
        String actual = controller.main(model, session);

        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verifyNoMoreInteractions(model);
        assertEquals(PAGE_MAIN, actual);
    }

    @Test
    public void redirectTestWithLink() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
        String link = "mail.ru";
        Mockito.when(service.visitLink(Mockito.anyString(),Mockito.anyMap())).thenReturn(link);

        //when
        controller.service = service;
        String actual = controller.redirect(model, request);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(service).visitLink(Mockito.anyString(),Mockito.anyMap());
        Mockito.verifyNoMoreInteractions(request, model, service);
        assertEquals("redirect://" + link, actual);
    }


    @Test
    public void redirectTestWithLink2() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
        String link = "http://mail.ru";
        Mockito.when(service.visitLink(Mockito.anyString(),Mockito.anyMap())).thenReturn(link);

        //when
        controller.service = service;
        String actual = controller.redirect(model, request);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(service).visitLink(Mockito.anyString(),Mockito.anyMap());
        Mockito.verifyNoMoreInteractions(request, model, service);
        assertEquals("redirect:" + link, actual);
    }

    @Test
    public void redirectTestWithText() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
        String text = "hello";
        Mockito.when(service.visitLink(Mockito.anyString(),Mockito.anyMap())).thenReturn(text);

        //when
        controller.service = service;
        String actual = controller.redirect(model, request);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(service).visitLink(Mockito.anyString(),Mockito.anyMap());
        Mockito.verify(model).addAttribute(ATTRIBUTE_MESSAGE, text);
        Mockito.verifyNoMoreInteractions(request, model, service);
        assertEquals(PAGE_MESSAGE, actual);
    }

    @Test
    public void redirectTestWithNonExistingLink() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
        String text = null;
        Mockito.when(service.visitLink(Mockito.anyString(),Mockito.anyMap())).thenReturn(text);

        //when
        controller.service = service;
        String actual = controller.redirect(model, request);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(service).visitLink(Mockito.anyString(),Mockito.anyMap());
        Mockito.verify(model).addAttribute(Mockito.anyString(),Mockito.anyString());
        Mockito.verifyNoMoreInteractions(request, model, service);
        assertEquals(PAGE_ERROR, actual);
    }

    @Test
    public void openImageTestExistingFile() throws IOException {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        Model model = Mockito.mock(Model.class);
        String fileName="AA"+IMAGE_EXTENSION;
        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
        Mockito.when(request.getServletContext()).thenReturn(context);
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(context.getRealPath("")).thenReturn(path);
        Mockito.when(response.getOutputStream()).thenReturn(os);

        Mockito.doNothing().when(os).write(Mockito.anyByte());
        createDirAndFile(path+"/resources",fileName);

        //when
        controller.openImage(model,request, response);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(request).getServletContext();
        Mockito.verify(response).getOutputStream();
        Mockito.verify(context).getRealPath(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(request, response, context);
        deleteDirAndFile(path+"/resources",fileName);
    }

    @Test
    public void openImageTestNonExistingLocalFile() throws IOException {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        Model model = Mockito.mock(Model.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        String fileName="AA"+IMAGE_EXTENSION;
        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
        Mockito.when(request.getServletContext()).thenReturn(context);
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(context.getRealPath("")).thenReturn(path);
        Mockito.when(response.getOutputStream()).thenReturn(os);
//        Mockito.doAnswer(new Answer<Boolean>() {
//            public Boolean answer(InvocationOnMock invocation) throws IOException {
//                createDirAndFile(path+"/resources",fileName);
//                return true;
//            }
//        }).when(service).downloadImageFromFTP(Mockito
//                .anyString(), Mockito
//                .anyString());

        Mockito.doNothing().when(os).write(Mockito.anyByte());

        //when
        controller.service = service;
        controller.openImage(model,request, response);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(request).getServletContext();
        Mockito.verify(response).getOutputStream();
        Mockito.verify(context).getRealPath(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(request, response, context,service);
        deleteDirAndFile(path+"/resources",fileName);
    }

    @Test
    public void openImageTestNonExistingLocalAndFTPFile() throws IOException {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        Model model = Mockito.mock(Model.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        PrintWriter printWriter = Mockito.mock(PrintWriter.class);
        String fileName="AA"+IMAGE_EXTENSION;
        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
        Mockito.when(request.getServletContext()).thenReturn(context);
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(context.getRealPath("")).thenReturn(path);
        Mockito.when(response.getOutputStream()).thenReturn(os);
        Mockito.when(response.getWriter()).thenReturn(printWriter);
//        Mockito.when(service.downloadImageFromFTP(Mockito
//                .anyString(), Mockito
//                .anyString())).thenReturn(false);

        Mockito.doNothing().when(os).write(Mockito.anyByte());
        Mockito.doNothing().when(printWriter).write(Mockito.anyString());

        //when
        controller.service = service;
        controller.openImage(model,request, response);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(request).getServletContext();
        Mockito.verify(context).getRealPath(Mockito.anyString());
        Mockito.verify(response).setContentType(Mockito.anyString());
        Mockito.verify(response).getWriter();
        Mockito.verify(printWriter).write(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(request, response, context,service);
        deleteDirAndFile(path+"/resources",fileName);
    }

    private void createDirAndFile(String path,String fileName) throws IOException {
        File dir = new File(path);
        dir.mkdir();
        File file = new File(path+"/" +fileName);
        file.createNewFile();
    }

    private void deleteDirAndFile(String path,String fileName) throws IOException {
        File dir = new File(path );
        File file = new File(path +"/"+ fileName);
        file.delete();
        dir.delete();
    }

    @Test
    public void openIconTest() throws IOException {
        //given
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        String fileName="favicon"+ICON_EXTENSION;
        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
        Mockito.when(request.getServletContext()).thenReturn(context);
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(context.getRealPath("")).thenReturn(path);
        Mockito.when(response.getOutputStream()).thenReturn(os);

        Mockito.doNothing().when(os).write(Mockito.anyByte());
        File dir = new File(path+"/resources");
        dir.mkdir();
        createDirAndFile(path+"/resources/images",fileName);

        //when
        controller.openIcon(request, response);

        //then
        Mockito.verify(request).getServletPath();
        Mockito.verify(request).getServletContext();
        Mockito.verify(response).getOutputStream();
        Mockito.verify(context).getRealPath(Mockito.anyString());
        Mockito.verifyNoMoreInteractions(request, response, context);
        deleteDirAndFile(path+"/resources/",fileName);
        dir.delete();
    }

    @Test
    public void createShortLinkTest() {
        //given
        String shortLink = "AA";
        String link = "yandex.ru";
        String context = "http:/localhost:8080/";
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        User user = new User.Builder()
                .addUserName("admin")
                .addIsAdmin(true)
                .addIsVerified(true)
                .build();
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
        Mockito.when(request.getServletContext()).thenReturn(servletContext);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
                (context));
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);

        //when
        controller.service=service;
        String actual=controller.createShortLink(model,session,request);
        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
        Mockito.verify(request,Mockito.times(2)).getRequestURL();
        Mockito.verify(request).getServletContext();
        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
        Mockito.verify(service).createShortLink(user,link,path,context,Mockito.anyMap());
        Mockito.verify(model,Mockito.times(4)).addAttribute(Mockito.anyString(),Mockito.anyObject
                ());
        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
        assertEquals(PAGE_MAIN,actual);
    }

    @Test
    public void createShortLinkTestWithoutAutorization() {
        //given
        String shortLink = "AA";
        String link = "yandex.ru";
        String context = "http:/localhost:8080/";
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(null);
        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
        Mockito.when(request.getServletContext()).thenReturn(servletContext);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
                (context));
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);
        Mockito.when(service.createShortLink(null,link,path,
                context,Mockito.anyMap()))
                .thenReturn(shortLink);
        //when
        controller.service=service;
        String actual=controller.createShortLink(model,session,request);
        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
        Mockito.verify(request,Mockito.times(2)).getRequestURL();
        Mockito.verify(request).getServletContext();
        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
        Mockito.verify(service).createShortLink(null,link,path,context,Mockito.anyMap());
        Mockito.verify(model,Mockito.times(4)).addAttribute(Mockito.anyString(),Mockito.anyObject
                ());
        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
        assertEquals(PAGE_MAIN,actual);
    }

    @Test
    public void createShortLinkTestWithoutLink() {
        //given
        String link = null;
        String context = "http:/localhost:8080/";
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        User user = new User.Builder()
                .addUserName("admin")
                .addIsAdmin(true)
                .addIsVerified(true)
                .build();        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);

        //when
        controller.service=service;
        String actual=controller.createShortLink(model,session,request);
        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
                Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
        assertEquals(PAGE_MAIN,actual);
    }

    @Test
    public void createShortLinkTestWithoutShortLink() {
        //given
        String shortLink = null;
        String link = "yandex.ru";
        String context = "http:/localhost:8080/";
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        LinksService service = Mockito.mock(LinksService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        User user = new User.Builder()
                .addUserName("admin")
                .addIsAdmin(true)
                .addIsVerified(true)
                .build();
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
        Mockito.when(request.getServletContext()).thenReturn(servletContext);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
                (context));
        String path = RootControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath();
        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);
        Mockito.when(service.createShortLink(user,link,path,
                context,Mockito.anyMap()))
                .thenReturn(shortLink);
        //when
        controller.service=service;
        String actual=controller.createShortLink(model,session,request);
        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
        Mockito.verify(request,Mockito.times(1)).getRequestURL();
        Mockito.verify(request).getServletContext();
        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
        Mockito.verify(service).createShortLink(user,link,path,context,Mockito.anyMap());
        Mockito.verify(model,Mockito.times(1)).addAttribute(Mockito.anyString(),Mockito.anyObject
                ());
        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
        assertEquals(PAGE_ERROR,actual);
    }
}
