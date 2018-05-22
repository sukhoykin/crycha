package name.sukhoykin.cryptic.command;

import javax.websocket.DecodeException;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandDispatcher;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.CommandMessage;
import name.sukhoykin.cryptic.MessageDecoder;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class EnvelopeCommand implements CommandHandler<EnvelopeMessage> {

    private final MessageDecoder decoder = new MessageDecoder();
    private final CommandDispatcher dispatcher;

    public EnvelopeCommand(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void handleMessage(ServiceDomain service, ClientSession client, EnvelopeMessage message)
            throws CommandException {

        byte[] payload = Hex.decode(message.getPayload());
        byte[] signature = Hex.decode(message.getSignature());

        if (!client.verifyPayload(payload, signature)) {
            throw new ProtocolException(ClientCloseCode.INVALID_SIGNATURE);
        }

        payload = client.decryptPayload(payload);

        CommandMessage commandMessage;
        try {
            commandMessage = decoder.decode(new String(payload));
        } catch (DecodeException e) {
            throw new CommandException(e);
        }

        dispatcher.dispatchMessage(service, client, commandMessage);
    }
}
