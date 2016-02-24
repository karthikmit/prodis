package com.buyhatke.utils;

/**
 * Hashing is a Utils class which exposes methods for hashing needs of the lib.
 */
public class Hashing {

    /**
     * Right now, we use Java in-built hashing for String.
     * TODO: Need to check better hashing for Keys.
     * @param key to be hashed.
     * @return Integer hash of the key.
     */
    public static Integer hash(String key) {
        return key.hashCode();
    }
}
