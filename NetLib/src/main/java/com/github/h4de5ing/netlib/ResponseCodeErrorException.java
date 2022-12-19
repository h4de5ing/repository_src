package com.github.h4de5ing.netlib;

public class ResponseCodeErrorException extends Exception {
    public ResponseCodeErrorException() {
    }

    public ResponseCodeErrorException(String message) {
        super(message);
    }

    public ResponseCodeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
