package name.sukhoykin.cryptic;

import java.util.HashMap;
import java.util.Map;

import name.sukhoykin.cryptic.exception.CommandException;

public class CommandDispatcher {

    private final Map<Class<?>, CommandHandler<?>> handlers = new HashMap<>();

    public void registerCommandHandler(Class<?> classOfCommand, CommandHandler<?> handler) {
        handlers.put(classOfCommand, handler);
    }

    public <T extends CommandHandler<?>> T getCommandHandler(Class<T> classOfHandler) {

        for (Map.Entry<Class<?>, CommandHandler<?>> entry : handlers.entrySet()) {

            @SuppressWarnings("unchecked")
            T handler = (T) entry.getValue();

            if (classOfHandler.isInstance(handler)) {
                return handler;
            }
        }

        throw new IllegalArgumentException("Unsupported handler class: " + classOfHandler.getName());
    }

    public void dispatchCommand(ServiceDomain service, ClientSession client, CommandMessage command)
            throws CommandException {

        @SuppressWarnings("unchecked")
        CommandHandler<CommandMessage> handler = (CommandHandler<CommandMessage>) handlers.get(command.getClass());

        if (handler == null) {
            throw new CommandException("Unsupported command: " + command.getCommand());
        }

        handler.handleCommand(service, client, command);
    }
}
