package name.sukhoykin.cryptic.exception;

import javax.websocket.CloseReason.CloseCode;

public class ProtocolException extends CommandException {

    private CloseCode closeCode;

    public ProtocolException(CloseCode closeCode, String message) {
        super(message);
        this.closeCode = closeCode;
    }

    public ProtocolException(CloseCode closeCode) {
        this(closeCode, closeCode.toString());
    }

    public CloseCode getCloseCode() {
        return closeCode;
    }
}
