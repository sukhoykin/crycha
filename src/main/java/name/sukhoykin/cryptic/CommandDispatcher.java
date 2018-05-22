package name.sukhoykin.cryptic;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.exception.CommandException;

public class CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final Map<Class<? extends CommandMessage>, CommandHandler<?>> commands = new HashMap<>();

    public void registerCommand(Class<? extends CommandMessage> classOfMessage, CommandHandler<?> command) {
        commands.put(classOfMessage, command);
    }

    public void dispatchMessage(ServiceDomain service, ClientSession client, CommandMessage message)
            throws CommandException {

        @SuppressWarnings("unchecked")
        CommandHandler<CommandMessage> command = (CommandHandler<CommandMessage>) commands.get(message.getClass());

        if (command == null) {
            throw new CommandException("Unsupported command: " + message.getCommand());
        }

        log.debug("#{} {} {}",
                new Object[] { client.getSession().getId(), command.getClass().getSimpleName(), message });
        command.handleMessage(service, client, message);
    }
}
