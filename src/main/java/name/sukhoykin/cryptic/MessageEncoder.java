package name.sukhoykin.cryptic;

import javax.websocket.EncodeException;
import javax.websocket.Encoder.Text;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class MessageEncoder implements Text<CommandMessage> {

    private final Gson gson = new Gson();

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String encode(CommandMessage command) throws EncodeException {
        return gson.toJson(command);
    }
}
