package name.sukhoykin.cryptic.command;

import javax.websocket.DecodeException;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandDispatcher;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.MessageDecoder;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class EnvelopeCommand extends CommandDispatcher implements CommandHandler<EnvelopeMessage> {

    private final MessageDecoder decoder = new MessageDecoder();

    @Override
    public void handleMessage(ServiceDomain service, ClientSession client, EnvelopeMessage message)
            throws CommandException {

        byte[] payload = Hex.decode(message.getPayload());
        byte[] signature = Hex.decode(message.getSignature());

        if (!client.verifyPayload(payload, signature)) {
            throw new ProtocolException(ClientCloseCode.CLIENT_INVALID_SIGNATURE);
        }

        payload = client.decryptPayload(payload);

        try {

            dispatchMessage(service, client, decoder.decode(new String(payload)));

        } catch (DecodeException e) {
            throw new CommandException(e);
        }
    }
}
