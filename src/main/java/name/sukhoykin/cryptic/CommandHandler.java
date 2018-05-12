package name.sukhoykin.cryptic;

public interface CommandHandler<T extends CommandMessage> {

    public void handleCommand(ServiceDomain service, ClientSession client, T command) throws CommandException;
}
