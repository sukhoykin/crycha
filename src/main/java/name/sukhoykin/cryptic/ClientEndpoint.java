package name.sukhoykin.cryptic;

import javax.websocket.Session;

public class ClientEndpoint {

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
}
