package org.xblackcat.sjpu.storage;

/**
 * @author xBlackCat Date: 27.07.11
 */
public class StorageException extends Exception {
    public StorageException() {
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
