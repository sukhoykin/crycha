package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class ProhibitMessage extends CommandMessage {
    
    public static final String NAME = "prohibit";

    public ProhibitMessage(String command) {
        super(NAME);
    }

    
}
