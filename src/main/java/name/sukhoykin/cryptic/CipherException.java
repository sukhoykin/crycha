package name.sukhoykin.cryptic;

public class CipherException extends Exception {

    public CipherException() {
        super();
    }

    public CipherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CipherException(String message, Throwable cause) {
        super(message, cause);
    }

    public CipherException(String message) {
        super(message);
    }

    public CipherException(Throwable cause) {
        super(cause);
    }
}
