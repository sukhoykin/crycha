package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class ProhibitMessage extends CommandMessage {

    public static final String NAME = "prohibit";

    private String email;

    public ProhibitMessage(String command) {
        super(NAME);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
