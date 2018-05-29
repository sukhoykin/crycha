package name.sukhoykin.cryptic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.exception.CommandException;

public abstract class ServiceHandler<T> implements CommandHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(ServiceHandler.class);

    private final Set<String> defaultSet = new HashSet<>();

    private enum Domain {
        INSTANCE;
        private final ConcurrentMap<String, ServiceSession> clients = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Set<String>> authorizations = new ConcurrentHashMap<>();
    }

    protected ConcurrentMap<String, ServiceSession> getClients() {
        return Domain.INSTANCE.clients;
    }

    protected ServiceSession getClient(String email) {
        return Domain.INSTANCE.clients.get(email);
    }

    protected ConcurrentMap<String, Set<String>> getAuthorizations() {
        return Domain.INSTANCE.authorizations;
    }

    protected Set<String> getAuthorization(String email) {
        return Domain.INSTANCE.authorizations.getOrDefault(email, defaultSet);
    }

    @Override
    public void onMessage(ServiceSession session, T message) throws CommandException {

        if (log.isDebugEnabled()) {
            LoggerFactory.getLogger(ServiceHandler.class).debug("clients: {} {}", Domain.INSTANCE.clients.keySet());
            LoggerFactory.getLogger(ServiceHandler.class).debug("authorization: {} {}", Domain.INSTANCE.authorizations);
        }
    }
}
