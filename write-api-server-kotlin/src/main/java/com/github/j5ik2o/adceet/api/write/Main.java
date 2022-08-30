package com.github.j5ik2o.adceet.api.write;

public class Main {
    public static void main(String[] args) {
        try {
            var clazz = Class.forName("com.github.j5ik2o.adceet.api.write.MainKt");
            var method = clazz.getDeclaredMethod("main");
            method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}