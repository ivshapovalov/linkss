package ru.ivan.linkss.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ru.ivan.linkss.service.LinksService;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ScheduledTasksTest {

    ScheduledTasks tasks;

    @Before
    public void setUp(){
        tasks=new ScheduledTasks();
    }

    @Test
    public void updateFreeLinksTestOk() throws Exception {
        //given
        LinksService service= Mockito.mock(LinksService.class);
        Mockito.when(service.updateFreeLinks()).thenReturn(BigInteger.ONE);
        //when
        tasks.service=service;
        tasks.updateFreeLinks();

        //then
        Mockito.verify(service).updateFreeLinks();
        Mockito.verifyNoMoreInteractions(service);
    }
    @Test
    public void updateFreeLinksTestException() throws Exception {
        //given
        LinksService service= Mockito.mock(LinksService.class);
        Mockito.doThrow(new Exception()).when(service).updateFreeLinks();
        //when
        tasks.service=service;
        tasks.updateFreeLinks();

        //then
        assertEquals(null,Mockito.verify(service).updateFreeLinks());
        Mockito.verifyNoMoreInteractions(service);
    }
    
    @Test
    public void checkExpiredUserLinksOk() throws Exception {
        //given
        LinksService service= Mockito.mock(LinksService.class);
        Mockito.when(service.deleteExpiredUserLinks()).thenReturn(BigInteger.ONE);

        //when
        tasks.service=service;
        tasks.checkExpiredUserLinks();

        //then
        Mockito.verify(service).deleteExpiredUserLinks();
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    public void checkExpiredUserLinksException() throws Exception {
        //given
        LinksService service= Mockito.mock(LinksService.class);
        Mockito.doThrow(new Exception()).when(service).deleteExpiredUserLinks();

        //when
        tasks.service=service;
        tasks.checkExpiredUserLinks();

        //then
        Mockito.verify(service).deleteExpiredUserLinks();
        Mockito.verifyNoMoreInteractions(service);
    }
}
