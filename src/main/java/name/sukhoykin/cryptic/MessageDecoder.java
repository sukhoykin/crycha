package name.sukhoykin.cryptic;

import java.security.SignatureException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import name.sukhoykin.cryptic.command.EnvelopeMessage;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class MessageDecoder implements Decoder.Text<CommandMessage> {

    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CommandMessage.class, new MessageDeserializer());
        gson = builder.create();
    }

    private EndpointConfig config;

    @Override
    public void init(EndpointConfig config) {
        this.config = config;
    }

    @Override
    public CommandMessage decode(String s) throws DecodeException {

        try {

            CommandMessage message = gson.fromJson(s, CommandMessage.class);

            if (message instanceof EnvelopeMessage) {

                MessageCipher cipher = (MessageCipher) config.getUserProperties().get("cipher");
                MessageSigner signer = (MessageSigner) config.getUserProperties().get("signer");

                if (cipher == null || signer == null) {
                    throw new DecodeException(s, "Secure session is not established",
                            new ProtocolException(CloseCode.CLIENT_INVALID_PROTOCOL));
                }

                EnvelopeMessage envelope = (EnvelopeMessage) message;

                byte[] payload = envelope.getPayload();
                byte[] signature = envelope.getSignature();

                if (payload == null || signature == null) {
                    throw new DecodeException(s, "Empty payload or signature",
                            new ProtocolException(CloseCode.CLIENT_INVALID_COMMAND));
                }

                try {

                    if (!signer.verify(payload, signature)) {
                        throw new DecodeException(s, "Message has invalid signature",
                                new ProtocolException(CloseCode.CLIENT_INVALID_SIGNATURE));
                    }

                    payload = cipher.decrypt(payload);

                } catch (CryptoException e) {

                    Throwable cause = e.getCause();

                    if (cause instanceof SignatureException) {
                        throw new DecodeException(s, cause.getMessage(),
                                new ProtocolException(CloseCode.CLIENT_INVALID_SIGNATURE));
                    } else {
                        throw new DecodeException(s, e.getMessage(), e);
                    }
                }

                message = gson.fromJson(new String(payload), CommandMessage.class);
            }

            return message;

        } catch (JsonParseException e) {

            Throwable cause = e.getCause();
            String message = cause != null ? cause.getMessage() : e.getMessage();

            throw new DecodeException(s, message, new ProtocolException(CloseCode.CLIENT_INVALID_COMMAND));
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void destroy() {
    }
}
