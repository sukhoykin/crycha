package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.SecureSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class ProhibitHandler implements CommandHandler<ProhibitMessage> {

    @Override
    public void onMessage(SecureSession session, ProhibitMessage message) throws CommandException {

    }
}
