package name.sukhoykin.cryptic;

public class CloseReason extends javax.websocket.CloseReason {

    public CloseReason(CloseCode closeCode, String reasonPhrase) {
        super(closeCode, reasonPhrase);
    }

    public CloseReason(CloseCode closeCode) {
        super(closeCode, closeCode.toString());
    }
}
