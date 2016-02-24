package com.buyhatke.prodis.exceptions;

/**
 * If the exception is not handled by application or generic ones, this exception shall be thrown.
 */
public class UnknownServerException extends RuntimeException {

    public UnknownServerException(String message) {
        super(message);
    }
}
