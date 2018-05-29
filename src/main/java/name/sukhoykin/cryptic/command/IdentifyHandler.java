package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CloseCode;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceSession;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class IdentifyHandler implements CommandHandler<IdentifyMessage> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void onMessage(ServiceSession session, IdentifyMessage message) throws CommandException {

        String email = message.getEmail();

        if (email == null || email.length() == 0) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_COMMAND, "Empty email address");
        }

        byte[] TOTP = session.identify(message.getEmail());
        log.debug("EMAIL {} TOTP {}", message.getEmail(), Hex.toHexString(TOTP));
    }
}
