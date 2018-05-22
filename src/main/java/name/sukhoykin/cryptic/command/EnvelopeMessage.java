package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class EnvelopeMessage extends CommandMessage {

    public static final String NAME = "envelope";

    private String payload;
    private String signature;

    public EnvelopeMessage() {
        super(NAME);
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
