package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class EnvelopeCommand extends CommandMessage {

    public static final String NAME = "envelope";

    private String payload;
    private String signature;

    public EnvelopeCommand() {
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
