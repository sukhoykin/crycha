package name.sukhoykin.cryptic;

public interface ServiceDomain {

    public <T extends CommandHandler<?>> T getCommandHandler(Class<T> classOfT);

    public void registerClient(ClientSession client);

    public ClientSession lookupClient(String clientId);

    public void unregisterClient(String clientId);
}
