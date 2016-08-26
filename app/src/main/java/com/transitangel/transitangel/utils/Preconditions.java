package com.transitangel.transitangel.utils;

/**
 * author yogesh.shrivastava.
 */
public class Preconditions {
    public static void checkNull(Object object) {
        if(object == null) {
            throw new NullPointerException();
        }
    }
}
