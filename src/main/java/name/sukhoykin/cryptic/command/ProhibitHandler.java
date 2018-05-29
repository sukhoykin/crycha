package name.sukhoykin.cryptic.command;

import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class ProhibitHandler extends ServiceHandler<ProhibitMessage> {

    @Override
    public void onMessage(ServiceSession session, ProhibitMessage message) throws CommandException {

        ServiceSession client = getClient(message.getEmail());

        if (client != null && !client.equals(session)) {

            if (getAuthorization(session.getEmail()).remove(message.getEmail())
                    && getAuthorization(message.getEmail()).remove(session.getEmail())) {

                message.setEmail(session.getEmail());

                try {

                    client.sendMessage(message);

                } catch (CommandException e) {
                    LoggerFactory.getLogger(ProhibitHandler.class).warn(
                            "Could not send prohibit command to client {}: {}", client.getEmail(), e.getMessage());
                }
            }

            super.onMessage(session, message);
        }
    }
}
