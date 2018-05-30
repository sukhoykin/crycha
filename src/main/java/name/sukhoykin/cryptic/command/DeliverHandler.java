package name.sukhoykin.cryptic.command;

import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class DeliverHandler extends ServiceHandler<DeliverMessage> {

    @Override
    public void onMessage(ServiceSession session, DeliverMessage message) throws CommandException {

        String sessionEmail = session.getEmail();
        String messageEmail = message.getEmail();

        if (getAuthorization(sessionEmail).contains(messageEmail)
                && getAuthorization(messageEmail).contains(sessionEmail)) {

            ServiceSession client = getClient(messageEmail);

            if (client != null) {

                message.setEmail(sessionEmail);

                try {

                    client.sendMessage(message);

                } catch (CommandException e) {
                    LoggerFactory.getLogger(AuthorizeHandler.class)
                            .warn("Could not send deliver command to client {}: {}", client.getEmail(), e.getMessage());
                }
            }
        }
    }
}
