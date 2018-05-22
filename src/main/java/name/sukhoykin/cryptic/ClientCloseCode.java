package name.sukhoykin.cryptic;

import javax.websocket.CloseReason.CloseCode;

public enum ClientCloseCode implements CloseCode {

    DUPLICATE_AUTHENTICATION(101), INVALID_COMMAND(400), INVALID_SIGNATURE(401), INVALID_KEY(402), SERVER_ERROR(500);

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
