package name.sukhoykin.cryptic;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

@ServerEndpoint("/api")
public class CrypticEndpoint {

    private Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("onOpen");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("onMessage");
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("onError " + error.getMessage());
    }
}
