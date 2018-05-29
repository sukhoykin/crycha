package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.CommandMessage;

public class EnvelopeMessage extends CommandMessage {

    public static final String NAME = "envelope";

    private String payload;
    private String signature;

    public EnvelopeMessage() {
        super(NAME);
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
