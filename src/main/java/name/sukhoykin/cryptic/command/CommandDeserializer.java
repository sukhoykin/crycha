package name.sukhoykin.cryptic.command;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CommandDeserializer implements JsonDeserializer<CommandMessage> {

    private Gson gson = new Gson();

    @Override
    public CommandMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        String command = json.getAsJsonObject().get("command").getAsString();

        switch (command) {
        case "identify":
            return gson.fromJson(json, IdentifyCommand.class);
        case "authenticate":
            return gson.fromJson(json, AuthenticateCommand.class);
        default:
            throw new JsonParseException("Invalid command: " + command);
        }
    }
}
