package name.sukhoykin.cryptic;

import name.sukhoykin.cryptic.exception.CommandException;

public interface CommandHandler<T> {

    public void onMessage(SecureSession session, T message) throws CommandException;
}
