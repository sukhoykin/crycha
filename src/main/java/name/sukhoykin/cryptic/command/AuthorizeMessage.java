package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.CommandMessage;

public class AuthorizeMessage extends CommandMessage {

    public static final String NAME = "authorize";

    private String email;
    private String dh;
    private String dsa;

    public AuthorizeMessage() {
        super(NAME);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDh(byte[] dh) {
        this.dh = Hex.toHexString(dh);
    }

    public byte[] getDh() {
        return dh != null ? Hex.decode(dh) : null;
    }

    public void setDsa(byte[] dsa) {
        this.dsa = Hex.toHexString(dsa);
    }

    public byte[] getDsa() {
        return dsa != null ? Hex.decode(dsa) : null;
    }
}
