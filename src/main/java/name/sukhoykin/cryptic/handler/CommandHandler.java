package name.sukhoykin.cryptic.handler;

import name.sukhoykin.cryptic.ClientService;
import name.sukhoykin.cryptic.command.CommandMessage;

public interface CommandHandler<T extends CommandMessage> {
    public void handleCommand(ClientService service, T command) throws CommandException;
}
