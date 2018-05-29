package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class CloseHandler extends ServiceHandler<CloseMessage> {

    @Override
    public void onMessage(ServiceSession session, CloseMessage message) throws CommandException {
        clients.remove(message.getEmail(), session);
    }
}
