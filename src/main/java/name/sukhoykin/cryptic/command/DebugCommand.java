package name.sukhoykin.cryptic.command;

public class DebugCommand extends CommandMessage {

    private String data;

    public DebugCommand(String command) {
        super("debug-" + command);
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
