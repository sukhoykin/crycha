package name.sukhoykin.cryptic;

abstract public class CommandMessage {

    private String command;

    public CommandMessage(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
