package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class AuthenticateMessage extends CommandMessage {

    public static final String NAME = "authenticate";

    private String dh;
    private String dsa;
    private String signature;

    public AuthenticateMessage() {
        super(NAME);
    }

    public void setDh(String dh) {
        this.dh = dh;
    }

    public String getDh() {
        return dh;
    }

    public void setDsa(String dsa) {
        this.dsa = dsa;
    }

    public String getDsa() {
        return dsa;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
