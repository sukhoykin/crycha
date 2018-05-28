package name.sukhoykin.cryptic;

public enum CloseCode implements javax.websocket.CloseReason.CloseCode {

    DUPLICATE_AUTHENTICATION(4100),
    CLIENT_ERROR(4400),
    CLIENT_INVALID_COMMAND(4401),
    CLIENT_INVALID_SIGNATURE(4402),
    CLIENT_INVALID_KEY(4403),
    CLIENT_INVALID_PROTOCOL(4404),
    SERVER_ERROR(4500),
    SERVER_INVALID_COMMAND(4501),
    SERVER_INVALID_SIGNATURE(4502),
    SERVER_INVALID_KEY(4503),
    SERVER_INVALID_PROTOCOL(4504);

    private int code;

    CloseCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name();
    }
}
