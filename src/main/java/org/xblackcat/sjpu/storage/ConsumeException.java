package org.xblackcat.sjpu.storage;

/**
 * 15.02.14 10:26
 *
 * @author xBlackCat
 */
public class ConsumeException extends Exception {
    public ConsumeException() {
    }

    public ConsumeException(String message) {
        super(message);
    }

    public ConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumeException(Throwable cause) {
        super(cause);
    }

    public ConsumeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
