package name.sukhoykin.cryptic;

public interface ServiceDomain {

    public void onAuthenticated(ClientSession client);

    public ClientSession lookupClient(String clientId);
}
