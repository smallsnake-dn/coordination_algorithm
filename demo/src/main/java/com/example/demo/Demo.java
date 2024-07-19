package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Demo {
    public static void main(String[] args) {
        // int[] arr = {1,2,3};
        // Map<Integer, HashMap<String, ArrayList<Object>>> rs = new HashMap<>();
        // for(int i : arr) {
        //     rs.computeIfAbsent(i, k -> new HashMap<>()).put("key", new ArrayList<>());
        // }
        // Map<String, Integer> map = new HashMap<>();
        // map.put("1", 1);

        // System.out.println(map.get("1"));

        Optional<String> s = Optional.of("test");
        Optional<Integer> r = s.map(String::length);
        System.out.println(r);

    }
    
}
