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

            String closeEmail = session.getEmail();

            for (String email : getAuthorizations().remove(closeEmail)) {

                if (getAuthorization(email).remove(closeEmail)) {

                    ServiceSession client = getClient(email);

                    if (client != null) {

                        try {

                            client.sendMessage(new CloseMessage(closeEmail));

                        } catch (CommandException e) {
                            LoggerFactory.getLogger(CloseHandler.class).warn(
                                    "Could not send close command to client {}: {}", client.getEmail(), e.getMessage());
                        }
                    }
                }
            }

            getAuthorizations().forEach((k, v) -> v.remove(closeEmail));
            getClients().remove(closeEmail, session);

            super.onMessage(session, message);
        }
    }
}
