package name.sukhoykin.cryptic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ServiceHandler<T> implements CommandHandler<T> {

    private static final ConcurrentMap<String, ServiceSession> clients = new ConcurrentHashMap<>();

    public ConcurrentMap<String, ServiceSession> getClients() {
        return clients;
    }
}
