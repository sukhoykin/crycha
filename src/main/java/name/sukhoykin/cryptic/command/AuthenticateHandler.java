package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.SecureSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class AuthenticateHandler implements CommandHandler<AuthenticateMessage> {

    @Override
    public void onMessage(SecureSession session, AuthenticateMessage message) throws CommandException {

        byte[] dh = message.getDh();
        byte[] dsa = message.getDsa();
        byte[] signature = message.getSignature();

        session.authenticate(dh, dsa, signature);

        // session = getSessions().put(session.getEmail(), session);
        //
        // if (session != null) {
        // session.close(new CloseReason(CloseCode.DUPLICATE_AUTHENTICATION));
        // }
    }
}
