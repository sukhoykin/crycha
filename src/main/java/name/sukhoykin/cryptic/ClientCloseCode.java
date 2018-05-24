package name.sukhoykin.cryptic;

import javax.websocket.CloseReason.CloseCode;

public enum ClientCloseCode implements CloseCode {

    DUPLICATE_AUTHENTICATION(4100),
    CLIENT_ERROR(4400),
    CLIENT_INVALID_COMMAND(4401),
    CLIENT_INVALID_SIGNATURE(4402),
    CLIENT_INVALID_KEY(4403),
    SERVER_ERROR(4500),
    SERVER_INVALID_COMMAND(4501),
    SERVER_INVALID_SIGNATURE(4502),
    SERVER_INVALID_KEY(4503);

    private int code;

    ClientCloseCode(int code) {
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
