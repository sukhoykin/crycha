package name.sukhoykin.cryptic;

public interface ServiceDomain {

    public void registerClient(ClientSession client);

    public ClientSession lookupClient(String clientId);

    public void unregisterClient(String clientId);
}
