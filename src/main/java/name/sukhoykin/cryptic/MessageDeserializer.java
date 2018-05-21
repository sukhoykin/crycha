package name.sukhoykin.cryptic;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import name.sukhoykin.cryptic.command.AuthenticateCommand;
import name.sukhoykin.cryptic.command.EnvelopeCommand;
import name.sukhoykin.cryptic.command.IdentifyCommand;

public class MessageDeserializer implements JsonDeserializer<CommandMessage> {

    private final Gson gson = new Gson();

    @Override
    public CommandMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        String command = json.getAsJsonObject().get("command").getAsString();

        switch (command) {
        case IdentifyCommand.NAME:
            return gson.fromJson(json, IdentifyCommand.class);
        case AuthenticateCommand.NAME:
            return gson.fromJson(json, AuthenticateCommand.class);
        case EnvelopeCommand.NAME:
            return gson.fromJson(json, EnvelopeCommand.class);
        default:
            throw new JsonParseException("Invalid command: " + command);
        }
    }
}
