package com.github.j5ik2o.adceet.api.write;

public class Main {
    public static void main(String[] args) {
        try {
            var clazz = Class.forName("com.github.j5ik2o.adceet.api.write.MainKt");
            var method = clazz.getDeclaredMethod("main", String[].class);
            method.invoke(null, new Object[]{ args });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}