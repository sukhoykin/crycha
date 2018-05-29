package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class CloseMessage extends CommandMessage {

    public static final String NAME = "close";

    private String email;

    public CloseMessage() {
        super(NAME);
    }

    public CloseMessage(String email) {
        super(NAME);
        this.email = email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
