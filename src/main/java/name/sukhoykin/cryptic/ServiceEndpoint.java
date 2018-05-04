package name.sukhoykin.cryptic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.command.AuthenticateCommand;
import name.sukhoykin.cryptic.command.CommandDecoder;
import name.sukhoykin.cryptic.command.CommandEncoder;
import name.sukhoykin.cryptic.command.CommandMessage;
import name.sukhoykin.cryptic.command.IdentifyCommand;
import name.sukhoykin.cryptic.handler.AuthenticateHandler;
import name.sukhoykin.cryptic.handler.CommandException;
import name.sukhoykin.cryptic.handler.CommandHandler;
import name.sukhoykin.cryptic.handler.IdentifyHandler;

@ServerEndpoint(value = "/api", encoders = { CommandEncoder.class }, decoders = { CommandDecoder.class })
public class ServiceEndpoint implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpoint.class);

    private final Map<Class<?>, CommandHandler<? extends CommandMessage>> dispatch = new HashMap<>();

    private final ThreadLocal<Session> session = new ThreadLocal<>();

    private final ConcurrentMap<Session, ClientEndpoint> clients = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ClientEndpoint> authenticatedClients = new ConcurrentHashMap<>();

    public ServiceEndpoint() {
        dispatch.put(IdentifyCommand.class, new IdentifyHandler());
        dispatch.put(AuthenticateCommand.class, new AuthenticateHandler());
    }

    private void dispatchCommand(CommandMessage command) throws CommandException {

        @SuppressWarnings("unchecked")
        CommandHandler<CommandMessage> handler = (CommandHandler<CommandMessage>) dispatch.get(command.getClass());

        if (handler == null) {
            throw new CommandException("Unsupported command: " + command.getCommand());
        }

        handler.handleCommand(this, command);
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("#{} Connected", session.getId());
        clients.put(session, new ClientEndpoint(session));
    }

    @OnMessage
    public void onMessage(Session session, CommandMessage command) {

        log.debug("#{} -> {}", session.getId(), command);

        this.session.set(session);

        try {
            dispatchCommand(command);
        } catch (CommandException e) {
            log.error("#{} {}", session.getId(), e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("#{} Disconnected", session.getId());
        clients.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("#{} {}", session.getId(), error.getMessage());
    }

    @Override
    public ClientEndpoint getClientEndpoint() {
        return clients.get(session.get());
    }
}
