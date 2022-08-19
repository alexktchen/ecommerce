package com.kt.ecommerce.utils;

import java.util.Random;

public class GenerateRandom {
    public static String randomNumbers(int length, int upperbound) {
        Random rand = new Random();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; i++) {
            res.append(rand.nextInt(upperbound));
        }
        return res.toString();
    }
}
