package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class DeliverHandler extends ServiceHandler<DeliverMessage> {

    @Override
    public void onMessage(ServiceSession session, DeliverMessage message) throws CommandException {
        super.onMessage(session, message);
    }
}
