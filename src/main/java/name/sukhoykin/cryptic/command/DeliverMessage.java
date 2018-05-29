package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.CommandMessage;

public class DeliverMessage extends CommandMessage {

    public static final String NAME = "deliver";

    private String email;
    private String payload;
    private String signature;

    public DeliverMessage() {
        super(NAME);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPayload(byte[] payload) {
        this.payload = Hex.toHexString(payload);
    }

    public byte[] getPayload() {
        return payload != null ? Hex.decode(payload) : null;
    }

    public void setSignature(byte[] signature) {
        this.signature = Hex.toHexString(signature);
    }

    public byte[] getSignature() {
        return signature != null ? Hex.decode(signature) : null;
    }
}
