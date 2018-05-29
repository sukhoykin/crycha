package name.sukhoykin.cryptic.command;

import javax.websocket.CloseReason;

import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class CloseHandler extends ServiceHandler<CloseMessage> {

    @Override
    public void onMessage(ServiceSession session, CloseMessage message) throws CommandException {

        if (session.isOpen()) {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Thank you for using the service"));
        } else {

            String email = session.getEmail();

            for (String clientEmail : authorization.remove(email)) {

                if (authorization.getOrDefault(clientEmail, defaultSet).remove(email)) {

                    ServiceSession client = clients.get(clientEmail);

                    if (client != null) {

                        try {

                            client.sendMessage(new CloseMessage(email));

                        } catch (CommandException e) {
                            LoggerFactory.getLogger(CloseHandler.class).warn(
                                    "Could not send close command to client {}: {}", client.getEmail(), e.getMessage());
                        }
                    }
                }
            }

            authorization.forEach((k, v) -> v.remove(email));

            clients.remove(email, session);
            LoggerFactory.getLogger(CloseHandler.class).debug("{} {}", clients.keySet(), authorization);
        }
    }
}
