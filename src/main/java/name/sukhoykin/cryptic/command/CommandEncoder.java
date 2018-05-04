package name.sukhoykin.cryptic.command;

import javax.websocket.EncodeException;
import javax.websocket.Encoder.Text;
import javax.websocket.EndpointConfig;

import com.google.gson.Gson;

public class CommandEncoder implements Text<CommandMessage> {

    private Gson gson = new Gson();

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String encode(CommandMessage object) throws EncodeException {
        return gson.toJson(object);
    }
}
