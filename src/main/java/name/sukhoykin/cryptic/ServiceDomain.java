package name.sukhoykin.cryptic;

public interface ServiceDomain {

    public <T extends CommandHandler<?>> T getCommandHandler(Class<T> classOfHandler);

    public void registerClient(ClientSession client);

    public ClientSession lookupClient(String clientId);

    public void unregisterClient(String clientId);
}
