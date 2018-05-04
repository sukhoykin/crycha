package name.sukhoykin.cryptic;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.command.CommandMessage;

public class ClientEndpoint {

    private final static Logger log = LoggerFactory.getLogger(ClientEndpoint.class);

    private Session session;
    private CipherSuite cipher = new CipherSuite();

    private String id;

    public ClientEndpoint(Session session) {
        this.session = session;
    }

    public CipherSuite getCipherSuite() {
        return cipher;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void sendCommand(CommandMessage command) throws IOException, EncodeException {

        session.getBasicRemote().sendObject(command);

        if (log.isDebugEnabled()) {
            log.debug("#{} <- {}", session.getId(), command);
        }
    }
}
