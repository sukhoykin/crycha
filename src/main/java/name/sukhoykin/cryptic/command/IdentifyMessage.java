package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class IdentifyMessage extends CommandMessage {
    
    public static final String NAME = "identify";

    private String email;

    public IdentifyMessage() {
        super(NAME);
    }

    public String getEmail() {
        return email;
    }
}
