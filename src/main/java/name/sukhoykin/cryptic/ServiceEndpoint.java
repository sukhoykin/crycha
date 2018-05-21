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

import name.sukhoykin.cryptic.command.AuthenticateCommand;
import name.sukhoykin.cryptic.command.AuthenticateHandler;
import name.sukhoykin.cryptic.command.EnvelopeCommand;
import name.sukhoykin.cryptic.command.EnvelopeHandler;
import name.sukhoykin.cryptic.command.IdentifyCommand;
import name.sukhoykin.cryptic.command.IdentifyHandler;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

@ServerEndpoint(value = "/api", encoders = { MessageEncoder.class }, decoders = {
        MessageDecoder.class }, configurator = ServiceInitializer.class)
public class ServiceEndpoint extends CommandDispatcher implements ServiceDomain {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpoint.class);

    private final ConcurrentMap<Session, ClientSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ClientSession> clients = new ConcurrentHashMap<>();

    public ServiceEndpoint() {
        registerCommandHandler(IdentifyCommand.class, new IdentifyHandler());
        registerCommandHandler(AuthenticateCommand.class, new AuthenticateHandler());
        registerCommandHandler(EnvelopeCommand.class, new EnvelopeHandler());
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("#{} Connected", session.getId());
        sessions.put(session, new ClientSession(session));
    }

    @OnMessage
    public void onMessage(Session session, CommandMessage command) {

        log.debug("#{} -> {}", session.getId(), command);

        ClientSession client = sessions.get(session);

        try {

            dispatchCommand(this, client, command);

        } catch (ProtocolException e) {
            log.warn("#{} {}", session.getId(), e.getMessage());
            closeClient(client, e.getCloseCode());

        } catch (CommandException e) {
            log.error("#{} {}", session.getId(), e.getMessage());
            closeClient(client, ClientCloseCode.SERVER_ERROR);
        }
    }

    private void closeClient(ClientSession client, CloseCode closeCode) {

        try {
            client.getSession().close(new CloseReason(closeCode, closeCode.toString()));
        } catch (IOException e) {
            log.error("", e);
            onClose(client.getSession());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("#{} Error", session.getId(), error);
        closeClient(sessions.get(session), ClientCloseCode.SERVER_ERROR);
    }

    @OnClose
    public void onClose(Session session) {

        log.debug("#{} Disconnected", session.getId());

        ClientSession client = sessions.remove(session);
        clients.remove(client.getClientId(), client);
    }

    @Override
    public void registerClient(ClientSession client) {

        client = clients.put(client.getClientId(), client);

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
