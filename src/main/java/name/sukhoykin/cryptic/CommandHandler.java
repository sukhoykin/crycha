package name.sukhoykin.cryptic;

import name.sukhoykin.cryptic.exception.CommandException;

public interface CommandHandler<T extends CommandMessage> {

    public void handleCommand(ServiceDomain service, ClientSession client, T command) throws CommandException;
}
