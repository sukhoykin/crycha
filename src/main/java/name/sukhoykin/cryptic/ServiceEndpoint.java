package name.sukhoykin.cryptic;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

import name.sukhoykin.cryptic.command.AuthenticateMessage;
import name.sukhoykin.cryptic.command.AuthenticateCommand;
import name.sukhoykin.cryptic.command.EnvelopeMessage;
import name.sukhoykin.cryptic.command.EnvelopeCommand;
import name.sukhoykin.cryptic.command.IdentifyMessage;
import name.sukhoykin.cryptic.command.IdentifyCommand;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

@ServerEndpoint(value = "/api", encoders = { MessageEncoder.class }, decoders = {
        MessageDecoder.class }, configurator = ServiceInitializer.class)
public class ServiceEndpoint extends CommandDispatcher implements ServiceDomain {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpoint.class);

    private final ConcurrentMap<Session, ClientSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ClientSession> clients = new ConcurrentHashMap<>();

    public ServiceEndpoint() {
        registerCommand(IdentifyMessage.class, new IdentifyCommand());
        registerCommand(AuthenticateMessage.class, new AuthenticateCommand());
        registerCommand(EnvelopeMessage.class, new EnvelopeCommand());
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("#{} Connected", session.getId());
        sessions.put(session, new ClientSession(session));
    }

    @OnMessage
    public void onMessage(Session session, CommandMessage message) {

        log.debug("#{} RECEIVE {}", session.getId(), message);

        ClientSession client = sessions.get(session);

        try {

            dispatchMessage(this, client, message);

        } catch (ProtocolException e) {
            log.warn("#{} {}", session.getId(), e.getMessage());
            closeClient(client, e.getCloseCode());

        } catch (CommandException e) {
            log.error("#{} {}", session.getId(), e.getMessage());
            closeClient(client, ClientCloseCode.SERVER_ERROR);
        }
    }

    private void closeClient(ClientSession client, CloseCode closeCode) {

        Session session = client.getSession();

        try {
            session.close(new CloseReason(closeCode, closeCode.toString()));
        } catch (IOException e) {
            log.error("Close error", e);
            onClose(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {

        log.error("#{} {}", session.getId(), error.getMessage(), error);

        if (error.getCause() instanceof JsonParseException) {
            closeClient(sessions.get(session), ClientCloseCode.INVALID_COMMAND);
        } else {
            closeClient(sessions.get(session), ClientCloseCode.SERVER_ERROR);
        }
    }

    @OnClose
    public void onClose(Session session) {

        log.debug("#{} Disconnected", session.getId());

        ClientSession client = sessions.remove(session);
        clients.remove(client.getEmail(), client);
    }

    @Override
    public void registerClient(ClientSession client) {

        client = clients.put(client.getEmail(), client);

        if (client != null) {
            closeClient(client, ClientCloseCode.DUPLICATE_AUTHENTICATION);
        }
    }

    @Override
    public ClientSession lookupClient(String clientId) {
        return clients.get(clientId);
    }

    @Override
    public void unregisterClient(String clientId) {

    }
}
