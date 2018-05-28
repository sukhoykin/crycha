package name.sukhoykin.cryptic.command;

import java.security.PublicKey;

import org.bouncycastle.jce.interfaces.ECPublicKey;

import name.sukhoykin.cryptic.ServiceHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeHandler extends ServiceHandler<AuthorizeMessage> {

    @Override
    public void onMessage(ServiceSession originator, AuthorizeMessage message) throws CommandException {

        ServiceSession recipient = getClients().get(message.getEmail());

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
