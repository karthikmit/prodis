package com.buyhatke.prodis.exceptions;

/**
 * No Key Found Exception shall be thrown if the queried key is either expired or not available in the cache.
 */

public class NoKeyFoundException extends RuntimeException {

    public NoKeyFoundException(String message) {
        super(message);
    }
}
