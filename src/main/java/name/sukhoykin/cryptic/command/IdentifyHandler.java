package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.SecureSession;
import name.sukhoykin.cryptic.exception.CommandException;

public class IdentifyHandler implements CommandHandler<IdentifyMessage> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void onMessage(SecureSession session, IdentifyMessage message) throws CommandException {

        byte[] TOTP = session.identify(message.getEmail());
        log.debug("EMAIL {} TOTP {}", message.getEmail(), Hex.toHexString(TOTP));
    }
}
