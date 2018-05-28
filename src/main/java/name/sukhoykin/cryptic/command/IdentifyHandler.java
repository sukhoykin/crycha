package name.sukhoykin.cryptic.command;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CloseCode;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServerSession;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class IdentifyHandler extends CommandHandler<IdentifyMessage> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    public IdentifyHandler(ServerSession session) {
        super(session);
    }

    @Override
    public void onMessage(ServerSession session, IdentifyMessage message) throws CommandException {

        if (session.getEmail() != null) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_PROTOCOL);
        }

        session.setEmail(message.getEmail());

        byte[] TOTP = session.generateTOTP();
        log.debug("EMAIL {} TOTP {}", message.getEmail(), Hex.toHexString(TOTP));

        DebugMessage debug = new DebugMessage();
        debug.setData(TOTP);

        session.sendMessage(debug);
    }
}
