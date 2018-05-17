package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class AuthorizeCommand extends CommandMessage {

    public static final String NAME = "authorize";

    private String email;

    public AuthorizeCommand() {
        super(NAME);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
