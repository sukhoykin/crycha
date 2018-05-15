package name.sukhoykin.cryptic;

import java.io.IOException;
import java.security.SecureRandom;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSession {

    private final static Logger log = LoggerFactory.getLogger(ClientSession.class);

    private final Session session;

    private final byte[] randomKey = new byte[16];
    private String clientId;

    public ClientSession(Session session) {
        this.session = session;
        new SecureRandom().nextBytes(randomKey);
    }

    public byte[] getRandomKey() {
        return randomKey;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void sendCommand(CommandMessage command) throws CommandException {

        try {

            session.getBasicRemote().sendObject(command);

            if (log.isDebugEnabled()) {
                log.debug("#{} <- {}", session.getId(), command);
            }

        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }
    }

    public void close(CloseCode closeCode) throws CommandException {

        try {
            session.close(new CloseReason(closeCode, closeCode.toString()));
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }
}
