package com.example.demo;

import java.util.*;

public class SetTest {
    public static void main(String[] args) {
        Set<long[]> set = new HashSet<>();
        long[] l1 = new long[2];
        l1[0] = 1;
        l1[1] = 3;
        long[] l2 = new long[2];
        l2[0] = 2;
        l2[1] = 5;
        long[] l3 = new long[2];
        l3[0] = 15;
        l3[1] = 20;
        long[] l4 = new long[2];
        l4[0] = 15;
        l4[1] = 20;
        long[] l5 = new long[2];
        l5[0] = 15;
        l5[1] = 18;
        List<long[]> lst = new ArrayList<>();
        lst.add(l2);
        lst.add(l1);
        lst.add(l3);

        merge(lst);

        System.out.println(new java.util.ArrayList(java.util.List.of(3,4)));
    }

    public static List<long[]> merge(List<long[]> lst) {
        List<long[]> rs = new ArrayList<>();
        lst.sort(Comparator.comparing(l -> l[0]));
        
        long min = lst.get(0)[0];
        long max = lst.get(0)[1];
        for(int i = 0; i < lst.size(); i++) {
            if(lst.get(i)[0] <= max) {
                max = lst.get(i)[1];
            } else {
                long[] rsl = new long[2];
                rsl[0] = min;
                rsl[1] = max;
                min = lst.get(i)[0];
                max = lst.get(i)[1];
                rs.add(rsl);
            }
            if(i + 1 == lst.size()) {
                long[] rsl = new long[2];
                rsl[0] = min;
                rsl[1] = max;
                min = lst.get(i)[0];
                max = lst.get(i)[1];
                rs.add(rsl);
            }
        }
        
        return rs;
    }
}
