package name.sukhoykin.cryptic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.MessageHandler;

import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public abstract class CommandHandler<T> implements MessageHandler.Whole<T> {

    private final ServerSession session;

    private static final ConcurrentMap<String, ServerSession> sessions = new ConcurrentHashMap<>();

    public CommandHandler(ServerSession session) {
        this.session = session;
    }

    public ConcurrentMap<String, ServerSession> getSessions() {
        return sessions;
    }

    @Override
    public void onMessage(T message) {

        try {

            onMessage(session, message);

        } catch (ProtocolException e) {
            session.close(new CloseReason(e.getCloseCode()));

        } catch (CommandException e) {
            session.close(new CloseReason(CloseCode.SERVER_ERROR));
        }
    }

    public abstract void onMessage(ServerSession session, T message) throws CommandException;
}
