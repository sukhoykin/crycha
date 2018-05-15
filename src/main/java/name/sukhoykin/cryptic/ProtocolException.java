package name.sukhoykin.cryptic;

import javax.websocket.CloseReason.CloseCode;

public class ProtocolException extends CommandException {

    private CloseCode closeCode;

    public ProtocolException(CloseCode closeCode) {
        super(closeCode.toString());
    }

    public CloseCode getCloseCode() {
        return closeCode;
    }
}
