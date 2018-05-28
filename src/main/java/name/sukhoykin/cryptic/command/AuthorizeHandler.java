package name.sukhoykin.cryptic.command;

import java.security.PublicKey;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServerSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeHandler extends CommandHandler<AuthorizeMessage> {

    public AuthorizeHandler(ServerSession session) {
        super(session);
    }

    @Override
    public void onMessage(ServerSession originator, AuthorizeMessage message) throws CommandException {

        ServerSession recipient = getSessions().get(message.getEmail());

        if (recipient != null) {

            byte[] dh = encodePublicKey(originator.getClientDh());
            byte[] dsa = encodePublicKey(originator.getClientDsa());

            message = new AuthorizeMessage();
            message.setEmail(originator.getEmail());
            message.setDh(dh);
            message.setDsa(dsa);

            recipient.sendMessage(message);
        }
    }

    private byte[] encodePublicKey(PublicKey key) {
        return ((ECPublicKey) key).getQ().getEncoded(true);
    }
}
