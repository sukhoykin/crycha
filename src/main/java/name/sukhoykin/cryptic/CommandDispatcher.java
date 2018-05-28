package name.sukhoykin.cryptic;

import java.util.HashMap;
import java.util.Map;

import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class CommandDispatcher {

    private Map<Class<? extends CommandMessage>, CommandHandler<? extends CommandMessage>> handlers = new HashMap<>();

    public <T extends CommandMessage> void addMessageHandler(Class<T> classOfMessage, CommandHandler<T> handler) {
        handlers.put(classOfMessage, handler);
    }

    public <T extends CommandMessage> void dispatchMessage(ServiceSession session, T message) throws CommandException {

        @SuppressWarnings("unchecked")
        CommandHandler<T> handler = (CommandHandler<T>) handlers.get(message.getClass());

        if (handler == null) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_PROTOCOL);
        }

        handler.onMessage(session, message);
    }

    public <T extends CommandMessage> void removeMessageHandler(Class<T> classOfMessage) {
        handlers.remove(classOfMessage);
    }
}
