package me.andrew28.addons.core;

/**
 * @author Andrew Tran
 */
public class NullExpressionException extends Exception {
    public NullExpressionException() {
    }

    public NullExpressionException(String message) {
        super(message);
    }

    public NullExpressionException(Throwable cause) {
        super(cause);
    }

    public NullExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
