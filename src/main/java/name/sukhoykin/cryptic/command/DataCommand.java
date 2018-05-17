package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class DataCommand extends CommandMessage {

    public static final String NAME = "data";

    private String payload;
    private String signature;

    public DataCommand() {
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
