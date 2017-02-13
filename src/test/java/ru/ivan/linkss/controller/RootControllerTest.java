package ru.ivan.linkss.controller;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import ru.ivan.linkss.repository.entity.User;
import ru.ivan.linkss.service.LinksService;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;

public class RootControllerTest {

    private static final String ATTRIBUTE_AUTORIZED_USER = "autorizedUser";
    private static final String PAGE_MAIN = "main";


    RootController controller;

    @Before
    public void setUp() {
        controller = new RootController();
    }

    @Test
    public void mainTestOk() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        User user = new User("admin", "", true, false);
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(user);

        //when
        String actual = controller.main(model, session);

        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verify(model).addAttribute(ATTRIBUTE_AUTORIZED_USER, user);
        Mockito.verifyNoMoreInteractions(service, model);
        assertEquals(PAGE_MAIN, actual);
    }
    @Test
    public void mainTestWithoutUser() {
        //given
        LinksService service = Mockito.mock(LinksService.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Model model = Mockito.mock(Model.class);
        Mockito.when(session.getAttribute(ATTRIBUTE_AUTORIZED_USER)).thenReturn(null);

        //when
        String actual = controller.main(model, session);

        //then
        Mockito.verify(session).getAttribute(ATTRIBUTE_AUTORIZED_USER);
        Mockito.verifyNoMoreInteractions(service, model);
        assertEquals(PAGE_MAIN, actual);
    }
}
