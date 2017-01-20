package ru.ivan.linkss;

import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ivan on 20.01.2017.
 */
public class MAin {
    public static void main(String[] args) {
        List<Integer> all= Arrays.asList(1,2,3,4,5,6);
        int number=10;
        int offset=10;
        List<Integer> list=all.stream()
                .skip(offset)
                .limit(number).collect(Collectors.toList());
        System.out.println(list);


    }

}
