package name.sukhoykin.cryptic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ServiceHandler<T> implements CommandHandler<T> {

    protected final Set<String> defaultSet = new HashSet<>();

    protected final ConcurrentMap<String, ServiceSession> clients = Domain.INSTANCE.clients;
    protected final ConcurrentMap<String, Set<String>> authorization = Domain.INSTANCE.authorization;

    private enum Domain {
        INSTANCE;
        private final ConcurrentMap<String, ServiceSession> clients = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Set<String>> authorization = new ConcurrentHashMap<>();
    }
}
