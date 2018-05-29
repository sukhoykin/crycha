package name.sukhoykin.cryptic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ServiceHandler<T> implements CommandHandler<T> {

    private enum Domain {
        INSTANCE;
        private final ConcurrentMap<String, ServiceSession> clients = new ConcurrentHashMap<>();
    }

    protected final ConcurrentMap<String, ServiceSession> clients = Domain.INSTANCE.clients;
}
