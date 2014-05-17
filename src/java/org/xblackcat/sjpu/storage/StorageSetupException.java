package org.xblackcat.sjpu.storage;

/**
 * 16.10.13 9:49
 *
 * @author xBlackCat
 */
public class StorageSetupException extends RuntimeException {
    public StorageSetupException() {
        super();
    }

    public StorageSetupException(String message) {
        super(message);
    }

    public StorageSetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageSetupException(Throwable cause) {
        super(cause);
    }

    protected StorageSetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
