package name.sukhoykin.cryptic;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.bouncycastle.util.encoders.Hex;

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

                byte[] payload = Hex.decode(envelope.getPayload());
                byte[] signature = Hex.decode(envelope.getSignature());

                try {

                    if (!signer.verify(payload, signature)) {
                        throw new DecodeException(s, "Message has invalid signature",
                                new ProtocolException(CloseCode.CLIENT_INVALID_SIGNATURE));
                    }

                    payload = cipher.decrypt(payload);

                } catch (CryptoException e) {
                    throw new DecodeException(s, e.getMessage(), e);
                }

                message = gson.fromJson(new String(payload), CommandMessage.class);
            }

            return message;

        } catch (JsonParseException e) {
            throw new DecodeException(s, e.getMessage(), new ProtocolException(CloseCode.CLIENT_INVALID_COMMAND));
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
