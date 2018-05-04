package name.sukhoykin.cryptic;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.command.AuthenticateCommand;
import name.sukhoykin.cryptic.command.CommandDecoder;
import name.sukhoykin.cryptic.command.CommandEncoder;
import name.sukhoykin.cryptic.command.CommandMessage;

@ServerEndpoint(value = "/api", encoders = { CommandEncoder.class }, decoders = { CommandDecoder.class })
public class CrypticServer {

    private static final Logger log = LoggerFactory.getLogger(CrypticServer.class);

    @OnOpen
    public void onOpen(Session session) {
        log.debug("#{} Connected", session.getId());
    }

    @OnMessage
    public void onMessage(Session session, CommandMessage command) {
        log.debug("#{} Command: {}", session.getId(), command.getCommand());
        try {
            session.getBasicRemote().sendObject(new AuthenticateCommand());
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("#{} Disconnected: ", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("#{} {}", session.getId(), error.getMessage());
    }
}
