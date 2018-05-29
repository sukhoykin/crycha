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

        session = clients.put(email, session);

        if (session != null) {
            CloseCode closeCode = CloseCode.DUPLICATE_AUTHENTICATION;
            session.close(new CloseReason(closeCode, closeCode.toString()));
        }

        authorization.put(email, ConcurrentHashMap.newKeySet());
        LoggerFactory.getLogger(AuthenticateHandler.class).debug("{} {}", clients.keySet(), authorization);
    }
}
