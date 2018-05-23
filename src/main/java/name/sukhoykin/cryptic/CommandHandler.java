package name.sukhoykin.cryptic;

import name.sukhoykin.cryptic.exception.CommandException;

public interface CommandHandler<T extends CommandMessage> {

    public void handleMessage(ServiceDomain service, ClientSession client, T message) throws CommandException;
}
