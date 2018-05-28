package name.sukhoykin.cryptic;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

import name.sukhoykin.cryptic.command.AuthenticateMessage;
import name.sukhoykin.cryptic.command.EnvelopeMessage;
import name.sukhoykin.cryptic.exception.CryptoException;

public class MessageEncoder implements Encoder.Text<CommandMessage> {

    private static final Gson gson = new Gson();
    private EndpointConfig config;

    @Override
    public void init(EndpointConfig config) {
        this.config = config;
    }

    @Override
    public String encode(CommandMessage message) throws EncodeException {

        MessageCipher cipher = (MessageCipher) config.getUserProperties().get("cipher");
        MessageSigner signer = (MessageSigner) config.getUserProperties().get("signer");

        if (cipher != null && signer != null && !(message instanceof AuthenticateMessage)) {

            try {

                byte[] payload = cipher.encrypt(gson.toJson(message).getBytes());
                byte[] signature = signer.sign(payload);

                EnvelopeMessage envelope = new EnvelopeMessage();
                envelope.setPayload(payload);
                envelope.setSignature(signature);

                message = envelope;

            } catch (CryptoException e) {
                throw new EncodeException(message, e.getMessage());
            }
        }

        return gson.toJson(message);
    }

    @Override
    public void destroy() {
    }
}
