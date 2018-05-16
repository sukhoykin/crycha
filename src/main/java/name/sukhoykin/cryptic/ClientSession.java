package name.sukhoykin.cryptic;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.exception.CommandException;

public class ClientSession {

    private final static Logger log = LoggerFactory.getLogger(ClientSession.class);

    private final Session session;
    private final byte[] randomKey = new byte[16];

    private String clientId;

    private PublicKey serverDsaKey;
    private PrivateKey clientDsaKey;
    private ClientCipher cipher;

    public ClientSession(Session session) {
        this.session = session;
        new SecureRandom().nextBytes(randomKey);
    }

    Session getSession() {
        return session;
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

    public void setServerDSAKey(PublicKey serverDsaKey) {
        this.serverDsaKey = serverDsaKey;
    }

    public PublicKey getServerDsaKey() {
        return serverDsaKey;
    }

    public void setClientDSAKey(PrivateKey clientDsaKey) {
        this.clientDsaKey = clientDsaKey;
    }

    public PrivateKey getClientDSAKey() {
        return clientDsaKey;
    }

    public void setCipher(ClientCipher cipher) {
        this.cipher = cipher;
    }

    public ClientCipher getCipher() {
        return cipher;
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
}
