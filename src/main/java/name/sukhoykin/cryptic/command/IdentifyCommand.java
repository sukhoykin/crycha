package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class IdentifyCommand extends CommandMessage {
    
    public static final String NAME = "identify";

    private String email;

    public IdentifyCommand() {
        super(NAME);
    }

    public String getEmail() {
        return email;
    }
}
