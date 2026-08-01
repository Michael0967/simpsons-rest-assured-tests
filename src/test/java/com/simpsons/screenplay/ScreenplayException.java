package com.simpsons.screenplay;

/**
 * Runtime exception raised when a Screenplay step cannot be completed.
 */
public class ScreenplayException extends RuntimeException {

    public ScreenplayException(String message, Throwable cause) {
        super(message, cause);
    }
}
