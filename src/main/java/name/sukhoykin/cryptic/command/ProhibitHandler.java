package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class ProhibitHandler implements CommandHandler<ProhibitMessage> {

    @Override
    public void onMessage(ServiceSession session, ProhibitMessage message) throws CommandException {

    }
}
