package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeCommand implements CommandHandler<AuthorizeMessage> {

    @Override
    public void handleMessage(ServiceDomain service, ClientSession client, AuthorizeMessage command)
            throws CommandException {
    }
}
