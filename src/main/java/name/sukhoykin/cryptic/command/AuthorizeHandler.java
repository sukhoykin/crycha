package name.sukhoykin.cryptic.command;

import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeHandler extends ServiceHandler<AuthorizeMessage> {

    @Override
    public void onMessage(ServiceSession session, AuthorizeMessage message) throws CommandException {

        String sessionEmail = session.getEmail();
        String messageEmail = message.getEmail();

        if (!sessionEmail.equals(messageEmail) && getAuthorization(sessionEmail).add(messageEmail)) {

            ServiceSession client = getClient(messageEmail);

            if (client != null) {

                message = new AuthorizeMessage();
                message.setEmail(sessionEmail);
                message.setDh(encodePublicKey(session.getClientDh()));
                message.setDsa(encodePublicKey(session.getClientDsa()));

                try {

                    client.sendMessage(message);

                } catch (CommandException e) {
                    LoggerFactory.getLogger(AuthorizeHandler.class).warn(
                            "Could not send authorize command to client {}: {}", client.getEmail(), e.getMessage());
                }
            }

            super.onMessage(session, message);
        }
    }
}
