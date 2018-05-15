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
import name.sukhoykin.cryptic.command.IdentifyCommand;
import name.sukhoykin.cryptic.handler.AuthenticateHandler;
import name.sukhoykin.cryptic.handler.IdentifyHandler;

@ServerEndpoint(value = "/api", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public class ServiceEndpoint extends CommandDispatcher implements ServiceDomain {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpoint.class);

    private final ConcurrentMap<Session, ClientSession> clients = new ConcurrentHashMap<>();

    public ServiceEndpoint() {
        registerCommandHandler(IdentifyCommand.class, new IdentifyHandler());
        registerCommandHandler(AuthenticateCommand.class, new AuthenticateHandler());
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("#{} Connected", session.getId());
        clients.put(session, new ClientSession(session));
    }

    @OnMessage
    public void onMessage(Session session, CommandMessage command) {

        log.debug("#{} -> {}", session.getId(), command);

        ClientSession client = clients.get(session);

        try {

            dispatchCommand(this, client, command);

        } catch (ProtocolException e) {
            log.warn("#{} {}", session.getId(), e.getMessage());
            closeSession(session, e.getCloseCode());

        } catch (CommandException e) {
            log.error("#{} {}", session.getId(), e.getMessage());
            closeSession(session, ClientCloseCode.SERVER_ERROR);
        }
    }

    private void closeSession(Session session, CloseCode closeCode) {

        try {
            session.close(new CloseReason(closeCode, closeCode.toString()));
        } catch (IOException e) {
            log.error("", e);
            onClose(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("#{} Error {}", session.getId(), error.getMessage());
        closeSession(session, ClientCloseCode.SERVER_ERROR);
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("#{} Disconnected", session.getId());
        clients.remove(session);
    }

    @Override
    public void registerClient(ClientSession client) {

    }

    @Override
    public ClientSession lookupClient(String clientId) {
        return null;
    }

    @Override
    public void unregisterClient(String clientId) {

    }
}
