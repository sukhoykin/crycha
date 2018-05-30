package name.sukhoykin.cryptic.command;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;

import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CloseCode;
import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class AuthenticateHandler extends ServiceHandler<AuthenticateMessage> {

    @Override
    public void onMessage(ServiceSession session, AuthenticateMessage message) throws CommandException {

        byte[] dh = message.getDh();
        byte[] dsa = message.getDsa();
        byte[] signature = message.getSignature();

        if (dh == null || dsa == null || signature == null) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_COMMAND, "Empty key or signature");
        }

        session.authenticate(dh, dsa, signature);

        String email = session.getEmail();

        ServiceSession closeSession = getClients().put(email, session);

        if (closeSession != null) {
            CloseCode closeCode = CloseCode.DUPLICATE_AUTHENTICATION;
            closeSession.close(new CloseReason(closeCode, closeCode.toString()));
        }

        getAuthorizations().put(email, ConcurrentHashMap.newKeySet());
        getAuthorizations().forEach((clientEmail, set) -> {

            ServiceSession client;

            if (set.contains(email) && (client = getClient(clientEmail)) != null) {

                AuthorizeMessage authorize = new AuthorizeMessage();
                authorize.setEmail(client.getEmail());
                authorize.setDh(encodePublicKey(client.getClientDh()));
                authorize.setDsa(encodePublicKey(client.getClientDsa()));

                try {

                    session.sendMessage(authorize);

                } catch (CommandException e) {
                    LoggerFactory.getLogger(AuthorizeHandler.class).warn(
                            "Could not send authorize command to client {}: {}", client.getEmail(), e.getMessage());
                }
            }
        });

        super.onMessage(session, message);
    }
}
