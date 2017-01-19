package ru.ivan.linkss;

import org.junit.Test;
import ru.ivan.linkss.util.Util;

import static org.junit.Assert.assertEquals;

public class AllTests {

    @Test
    public void testGetDomainName() {

        assertEquals("ya.ru", Util.getDomainName("www2.ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("http://ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("www.ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("https://ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("www3.ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("ftp://ya.ru/adfasfd"));
        assertEquals("ya.ru", Util.getDomainName("https://www.ya.ru/sadfaf"));

    }

    public static void main(String[] args) {
        boolean failed = false;
        do {
            if (!failed) {
                System.out.println("failed");
                //failed = !failed;
                continue;
            }
        } while (failed);
    }


}
