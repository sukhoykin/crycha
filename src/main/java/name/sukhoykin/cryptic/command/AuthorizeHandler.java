package name.sukhoykin.cryptic.command;

import java.security.PublicKey;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeHandler extends ServiceHandler<AuthorizeMessage> {

    @Override
    public void onMessage(ServiceSession session, AuthorizeMessage message) throws CommandException {

        ServiceSession client = getClient(message.getEmail());

        if (client != null && !client.equals(session) && getAuthorization(session.getEmail()).add(message.getEmail())) {

            byte[] dh = encodePublicKey(session.getClientDh());
            byte[] dsa = encodePublicKey(session.getClientDsa());

            message = new AuthorizeMessage();
            message.setEmail(session.getEmail());
            message.setDh(dh);
            message.setDsa(dsa);

            try {

                client.sendMessage(message);

            } catch (CommandException e) {
                LoggerFactory.getLogger(AuthorizeHandler.class)
                        .warn("Could not send authorize command to client {}: {}", client.getEmail(), e.getMessage());
            }

            super.onMessage(session, message);
        }
    }

    private byte[] encodePublicKey(PublicKey key) {
        return ((ECPublicKey) key).getQ().getEncoded(true);
    }
}
