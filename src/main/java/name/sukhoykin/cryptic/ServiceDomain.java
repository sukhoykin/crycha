package name.sukhoykin.cryptic;

import name.sukhoykin.cryptic.handler.CommandHandler;

public interface ServiceDomain {

    public <T extends CommandHandler<?>> T getCommandHandler(Class<T> classOfT);
    
    public void clientAuthenticated(String clientId, ClientSession client);
    
    public ClientSession lookupClient(String clientId);
    
    public void unregisterClient(String clientId);
}
