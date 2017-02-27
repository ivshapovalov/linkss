package ru.ivan.linkss.controller;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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

import static org.junit.Assert.assertEquals;

public class ActionsControllerTest {

    private static final String FILE_SEPARTOR = File.separator;
    private static final String RESOURCE_FOLDER = "resources";
    private static final String IMAGE_EXTENSION = ".png";

    private static final String WEB_SEPARTOR = "/";
    private static final String PAGE_ERROR = "error";
    private static final String PAGE_MESSAGE = "message";
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
    private static final String PAGE_ARCHIVE = "archive";
    private static final String ACTION_LOGOUT = "logout";

    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_RESTORE = "restore";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_POPULATE = "populate";
    private static final String ACTION_CHECK_EXPIRED = "checkExpired";

    private static final String ATTRIBUTE_USER = "user";
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

    ActionsController controller;

    @Before
    public void setUp() {
        controller = new ActionsController();
    }

    @Test
    public void registrationTestOk() throws IOException {
        //given
        Model model = Mockito.mock(Model.class);

        //when
        String actual = controller.registration(model);

        //then
        Mockito.verify(model).addAttribute(Mockito.anyString(), Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(model);
        assertEquals(PAGE_SIGNUP, actual);
    }

    @Test
    public void signinTestOk() throws IOException {
        //given
        Model model = Mockito.mock(Model.class);

        //when
        String actual = controller.signin(model);

        //then
        Mockito.verify(model).addAttribute(Mockito.anyString(), Mockito.any(User.class));
        Mockito.verifyNoMoreInteractions(model);
        assertEquals(PAGE_SIGNIN, actual);
    }

    @Test
    public void logoutTestOk() throws IOException {
        //given
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        //when
        String actual = controller.logout(model,session);

        //then
        Mockito.verify(model).addAttribute(ATTRIBUTE_AUTORIZED_USER,null);
        Mockito.verify(session).setAttribute(ATTRIBUTE_AUTORIZED_USER,null);
        Mockito.verifyNoMoreInteractions(session,model);
        assertEquals("redirect:/", actual);
    }
    @Test
    public void manageTestAdminRole() throws IOException {
        //given
        Model model = Mockito.mock(Model.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        User user = Mockito.mock(User.class);
        LinksService service= Mockito.mock(LinksService.class);

        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
        Mockito.when(user.isEmpty()).thenReturn(false);
        Mockito.when(user.isAdmin()).thenReturn(true);
        Mockito.when(service.getDBLinksSize()).thenReturn(0L);
        Mockito.when(service.getDBFreeLinksSize()).thenReturn(0L);
        Mockito.when(service.getDomainsSize(user)).thenReturn(0L);
        Mockito.when(service.getUsersSize(user)).thenReturn(0L);

        //when
        controller.service=service;
        String actual = controller.manage(model,session);

        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(user).isEmpty();
        Mockito.verify(user).isAdmin();
        Mockito.verify(model,Mockito.times(4)).addAttribute(Mockito.anyString(),Mockito.anyLong());
        Mockito.verify(service).getDBFreeLinksSize();
        Mockito.verify(service).getDBLinksSize();
        Mockito.verify(service).getDomainsSize(user);
        Mockito.verify(service).getUsersSize(user);
        Mockito.verifyNoMoreInteractions(session,model,service);
        assertEquals(PAGE_MANAGE, actual);
    }

//
//    @Test
//    public void mainTestWithoutUser() {
//        //given
//        HttpSession session = Mockito.mock(HttpSession.class);
//        Model model = Mockito.mock(Model.class);
//        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(null);
//
//        //when
//        String actual = controller.main(model, session);
//
//        //then
//        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
//        Mockito.verifyNoMoreInteractions(model);
//        assertEquals(PAGE_MAIN, actual);
//    }
//
//    @Test
//    public void redirectTestWithLink() {
//        //given
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        Model model = Mockito.mock(Model.class);
//        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
//        String link = "mail.ru";
//        Mockito.when(service.visitLink(Mockito.anyString())).thenReturn(link);
//
//        //when
//        controller.service = service;
//        String actual = controller.redirect(model, request);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(service).visitLink(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, model, service);
//        assertEquals("redirect://" + link, actual);
//    }
//
//
//    @Test
//    public void redirectTestWithLink2() {
//        //given
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        Model model = Mockito.mock(Model.class);
//        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
//        String link = "http://mail.ru";
//        Mockito.when(service.visitLink(Mockito.anyString())).thenReturn(link);
//
//        //when
//        controller.service = service;
//        String actual = controller.redirect(model, request);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(service).visitLink(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, model, service);
//        assertEquals("redirect:" + link, actual);
//    }
//
//    @Test
//    public void redirectTestWithText() {
//        //given
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        Model model = Mockito.mock(Model.class);
//        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
//        String text = "hello";
//        Mockito.when(service.visitLink(Mockito.anyString())).thenReturn(text);
//
//        //when
//        controller.service = service;
//        String actual = controller.redirect(model, request);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(service).visitLink(Mockito.anyString());
//        Mockito.verify(model).addAttribute(ATTRIBUTE_MESSAGE, text);
//        Mockito.verifyNoMoreInteractions(request, model, service);
//        assertEquals(PAGE_MESSAGE, actual);
//    }
//
//    @Test
//    public void redirectTestWithNonExistingLink() {
//        //given
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        Model model = Mockito.mock(Model.class);
//        Mockito.when(request.getServletPath()).thenReturn("http://yandex.ru/ghyjjh");
//        String text = null;
//        Mockito.when(service.visitLink(Mockito.anyString())).thenReturn(text);
//
//        //when
//        controller.service = service;
//        String actual = controller.redirect(model, request);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(service).visitLink(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, model, service);
//        assertEquals(PAGE_ERROR, actual);
//    }
//
//    @Test
//    public void openImageTestExistingFile() throws IOException {
//        //given
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
//        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
//        ServletContext context = Mockito.mock(ServletContext.class);
//        String fileName="AA"+IMAGE_EXTENSION;
//        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
//        Mockito.when(request.getServletContext()).thenReturn(context);
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(context.getRealPath("")).thenReturn(path);
//        Mockito.when(response.getOutputStream()).thenReturn(os);
//
//        Mockito.doNothing().when(os).write(Mockito.anyByte());
//        createDirAndFile(path+"/resources",fileName);
//
//        //when
//        controller.openImage(request, response);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(response).getOutputStream();
//        Mockito.verify(context).getRealPath(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, response, context);
//        deleteDirAndFile(path+"/resources",fileName);
//    }
//
//    @Test
//    public void openImageTestNonExistingFile() throws IOException {
//        //given
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
//        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
//        ServletContext context = Mockito.mock(ServletContext.class);
//        String fileName="AA"+IMAGE_EXTENSION;
//        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
//        Mockito.when(request.getServletContext()).thenReturn(context);
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(context.getRealPath("")).thenReturn(path);
//        Mockito.when(response.getOutputStream()).thenReturn(os);
//        Mockito.doAnswer(new Answer<String>() {
//            public String answer(InvocationOnMock invocation) throws IOException {
//                createDirAndFile(path+"/resources",fileName);
//                return "";
//            }
//        }).when(service).downloadImageFromFTP(Mockito
//                .anyString(), Mockito
//                .anyString());
//        Mockito.doNothing().when(os).write(Mockito.anyByte());
//
//        //when
//        controller.service = service;
//        controller.openImage(request, response);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(response).getOutputStream();
//        Mockito.verify(context).getRealPath(Mockito.anyString());
//        Mockito.verify(service).downloadImageFromFTP(Mockito.anyString(), Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, response, context,service);
//        deleteDirAndFile(path+"/resources",fileName);
//    }
//
//    private void createDirAndFile(String path,String fileName) throws IOException {
//        File dir = new File(path);
//        dir.mkdir();
//        File file = new File(path+"/" +fileName);
//        file.createNewFile();
//    }
//
//    private void deleteDirAndFile(String path,String fileName) throws IOException {
//        File dir = new File(path );
//        File file = new File(path +"/"+ fileName);
//        file.delete();
//        dir.delete();
//    }
//
//    @Test
//    public void openIconTest() throws IOException {
//        //given
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
//        ServletOutputStream os = Mockito.mock(ServletOutputStream.class);
//        ServletContext context = Mockito.mock(ServletContext.class);
//        String fileName="favicon"+ICON_EXTENSION;
//        Mockito.when(request.getServletPath()).thenReturn("/" + fileName);
//        Mockito.when(request.getServletContext()).thenReturn(context);
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(context.getRealPath("")).thenReturn(path);
//        Mockito.when(response.getOutputStream()).thenReturn(os);
//
//        Mockito.doNothing().when(os).write(Mockito.anyByte());
//        File dir = new File(path+"/resources");
//        dir.mkdir();
//        createDirAndFile(path+"/resources/images",fileName);
//
//        //when
//        controller.openIcon(request, response);
//
//        //then
//        Mockito.verify(request).getServletPath();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(response).getOutputStream();
//        Mockito.verify(context).getRealPath(Mockito.anyString());
//        Mockito.verifyNoMoreInteractions(request, response, context);
//        deleteDirAndFile(path+"/resources/",fileName);
//        dir.delete();
//    }
//
//    @Test
//    public void createShortLinkTest() {
//        //given
//        String shortLink = "AA";
//        String link = "yandex.ru";
//        String context = "http:/localhost:8080/";
//        Model model = Mockito.mock(Model.class);
//        HttpSession session = Mockito.mock(HttpSession.class);
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        ServletContext servletContext = Mockito.mock(ServletContext.class);
//        User user = new User("admin", "", true, false);
//        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
//        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
//        Mockito.when(request.getServletContext()).thenReturn(servletContext);
//        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
//                (context));
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);
//        Mockito.when(service.createShortLink(user,link,path,
//                context))
//                .thenReturn(shortLink);
//        //when
//        controller.service=service;
//        String actual=controller.createShortLink(model,session,request);
//        //then
//        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
//        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
//        Mockito.verify(request,Mockito.times(2)).getRequestURL();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
//        Mockito.verify(service).createShortLink(user,link,path,context);
//        Mockito.verify(model,Mockito.times(4)).addAttribute(Mockito.anyString(),Mockito.anyObject
//                ());
//        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
//        assertEquals(PAGE_MAIN,actual);
//    }
//
//    @Test
//    public void createShortLinkTestWithoutAutorization() {
//        //given
//        String shortLink = "AA";
//        String link = "yandex.ru";
//        String context = "http:/localhost:8080/";
//        Model model = Mockito.mock(Model.class);
//        HttpSession session = Mockito.mock(HttpSession.class);
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        ServletContext servletContext = Mockito.mock(ServletContext.class);
//        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(null);
//        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
//        Mockito.when(request.getServletContext()).thenReturn(servletContext);
//        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
//                (context));
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);
//        Mockito.when(service.createShortLink(null,link,path,
//                context))
//                .thenReturn(shortLink);
//        //when
//        controller.service=service;
//        String actual=controller.createShortLink(model,session,request);
//        //then
//        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
//        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
//        Mockito.verify(request,Mockito.times(2)).getRequestURL();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
//        Mockito.verify(service).createShortLink(null,link,path,context);
//        Mockito.verify(model,Mockito.times(4)).addAttribute(Mockito.anyString(),Mockito.anyObject
//                ());
//        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
//        assertEquals(PAGE_MAIN,actual);
//    }
//
//    @Test
//    public void createShortLinkTestWithoutLink() {
//        //given
//        String link = null;
//        String context = "http:/localhost:8080/";
//        Model model = Mockito.mock(Model.class);
//        HttpSession session = Mockito.mock(HttpSession.class);
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        ServletContext servletContext = Mockito.mock(ServletContext.class);
//        User user = new User("admin", "", true, false);
//        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
//        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
//
//        //when
//        controller.service=service;
//        String actual=controller.createShortLink(model,session,request);
//        //then
//        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
//        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
//                Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
//        assertEquals(PAGE_MAIN,actual);
//    }
//
//    @Test
//    public void createShortLinkTestWithoutShortLink() {
//        //given
//        String shortLink = null;
//        String link = "yandex.ru";
//        String context = "http:/localhost:8080/";
//        Model model = Mockito.mock(Model.class);
//        HttpSession session = Mockito.mock(HttpSession.class);
//        LinksService service = Mockito.mock(LinksService.class);
//        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
//        ServletContext servletContext = Mockito.mock(ServletContext.class);
//        User user = new User("admin", "", true, false);
//        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);
//        Mockito.when(request.getParameter(ATTRIBUTE_LINK)).thenReturn(link);
//        Mockito.when(request.getServletContext()).thenReturn(servletContext);
//        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer
//                (context));
//        String path = ActionsControllerTest.class.getProtectionDomain().getCodeSource().getLocation()
//                .getPath();
//        Mockito.when(servletContext.getRealPath(Mockito.anyString())).thenReturn(path);
//        Mockito.when(service.createShortLink(user,link,path,
//                context))
//                .thenReturn(shortLink);
//        //when
//        controller.service=service;
//        String actual=controller.createShortLink(model,session,request);
//        //then
//        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
//        Mockito.verify(request).getParameter(ATTRIBUTE_LINK);
//        Mockito.verify(request,Mockito.times(1)).getRequestURL();
//        Mockito.verify(request).getServletContext();
//        Mockito.verify(servletContext).getRealPath(Mockito.anyString());
//        Mockito.verify(service).createShortLink(user,link,path,context);
//        Mockito.verify(model,Mockito.times(1)).addAttribute(Mockito.anyString(),Mockito.anyObject
//                ());
//        Mockito.verifyNoMoreInteractions(model,session,request, service,servletContext);
//        assertEquals(PAGE_ERROR,actual);
//    }
}
