package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class DebugMessage extends CommandMessage {

    private String data;

    public DebugMessage(String command) {
        super("debug-" + command);
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
