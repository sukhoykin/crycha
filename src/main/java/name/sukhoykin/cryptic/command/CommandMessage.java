package name.sukhoykin.cryptic.command;

import com.google.gson.Gson;

abstract public class CommandMessage {

    private final transient Gson gson = new Gson();

    private String command;

    public CommandMessage(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
