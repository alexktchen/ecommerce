package com.kt.ecommerce.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    public static boolean isPhoneInvalid(String phone) {
        Pattern pattern = Pattern.compile("^(\\d{3}[- .]?){2}\\d{4}$");
        Matcher matcher = pattern.matcher("0912345678");
        return !matcher.matches();
    }
}
