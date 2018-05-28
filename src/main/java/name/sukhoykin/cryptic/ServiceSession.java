package name.sukhoykin.cryptic;

import java.security.PublicKey;

import javax.websocket.CloseReason;

import name.sukhoykin.cryptic.exception.CommandException;

public interface ServiceSession {

    public byte[] identify(String email) throws CommandException;

    public void authenticate(byte[] dh, byte[] dsa, byte[] signature) throws CommandException;

    public String getEmail();

    public PublicKey getClientDh();

    public PublicKey getClientDsa();

    public void sendMessage(CommandMessage message) throws CommandException;

    public void close(CloseReason reason);
}
