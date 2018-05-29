package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.CommandMessage;

public class AuthenticateMessage extends CommandMessage {

    public static final String NAME = "authenticate";

    private String dh;
    private String dsa;
    private String signature;

    public AuthenticateMessage() {
        super(NAME);
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

    public void setSignature(byte[] signature) {
        this.signature = Hex.toHexString(signature);
    }

    public byte[] getSignature() {
        return signature != null ? Hex.decode(signature) : null;
    }
}
