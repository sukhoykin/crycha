package name.sukhoykin.cryptic;

import name.sukhoykin.cryptic.exception.CommandException;

public interface CommandHandler<T extends CommandMessage> {

    public void onMessage(ServiceSession session, T message) throws CommandException;
}
