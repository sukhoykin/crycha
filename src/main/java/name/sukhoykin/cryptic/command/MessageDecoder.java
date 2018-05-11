package name.sukhoykin.cryptic.command;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class MessageDecoder implements Decoder.Text<CommandMessage> {

    private final Gson gson = new GsonBuilder().registerTypeAdapter(CommandMessage.class, new CommandDeserializer())
            .create();

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public CommandMessage decode(String s) throws DecodeException {

        try {
            return gson.fromJson(s, CommandMessage.class);
        } catch (JsonParseException e) {
            throw new DecodeException(s, e.getMessage(), e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
