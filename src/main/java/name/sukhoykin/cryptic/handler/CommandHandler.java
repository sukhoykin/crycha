package name.sukhoykin.cryptic.handler;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.command.CommandMessage;

public interface CommandHandler<T extends CommandMessage> {
    public void handleCommand(ServiceDomain service, ClientSession client, T command) throws CommandException;
}
