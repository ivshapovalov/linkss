package ru.ivan.linkss;


import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<A> listA = new ArrayList<>();
        List<B> listB = new ArrayList<>();
        List<C> listC = new ArrayList<>();
        List<D> listD = new ArrayList<>();

        listA.add(new A());
        listB.add(new B());
        listC.add(new C());
        listD.add(new D());
        process(listA);
        process(listB);
        process(listC);


    }

    static class A {
    }

    static class B extends A {
    }

    static class C extends B {
    }

    static class D extends C {
    }

    public static void process(List<? super C> list) {
//
//        list.add(new A());
//        list.add(new B());
        list.add(new C());
        list.add(new D());
    }

}
