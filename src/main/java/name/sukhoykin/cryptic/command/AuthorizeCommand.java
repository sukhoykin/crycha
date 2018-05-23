package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthorizeCommand implements CommandHandler<AuthorizeMessage> {

    @Override
    public void handleMessage(ServiceDomain service, ClientSession originator, AuthorizeMessage message)
            throws CommandException {

        ClientSession recipient = service.lookupClient(message.getEmail());

        if (recipient != null) {

            byte[] dh = originator.encodePublicKey(originator.getClientDHKey(), true);
            byte[] dsa = originator.encodePublicKey(originator.getClientDSAKey(), true);

            message = new AuthorizeMessage();
            message.setEmail(originator.getEmail());
            message.setDh(Hex.toHexString(dh));
            message.setDsa(Hex.toHexString(dsa));

            recipient.sendMessage(message);
        }
    }
}
