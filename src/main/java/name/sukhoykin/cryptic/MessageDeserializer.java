package name.sukhoykin.cryptic;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import name.sukhoykin.cryptic.command.AuthenticateMessage;
import name.sukhoykin.cryptic.command.AuthorizeMessage;
import name.sukhoykin.cryptic.command.CloseMessage;
import name.sukhoykin.cryptic.command.EnvelopeMessage;
import name.sukhoykin.cryptic.command.IdentifyMessage;
import name.sukhoykin.cryptic.command.ProhibitMessage;

public class MessageDeserializer implements JsonDeserializer<CommandMessage> {

    private final Gson gson = new Gson();

    @Override
    public CommandMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        String command = json.getAsJsonObject().get("command").getAsString();

        switch (command) {
        case IdentifyMessage.NAME:
            return gson.fromJson(json, IdentifyMessage.class);
        case AuthenticateMessage.NAME:
            return gson.fromJson(json, AuthenticateMessage.class);
        case EnvelopeMessage.NAME:
            return gson.fromJson(json, EnvelopeMessage.class);
        case AuthorizeMessage.NAME:
            return gson.fromJson(json, AuthorizeMessage.class);
        case ProhibitMessage.NAME:
            return gson.fromJson(json, ProhibitMessage.class);
        case CloseMessage.NAME:
            return gson.fromJson(json, CloseMessage.class);
        default:
            throw new JsonParseException("Invalid command: " + command);
        }
    }
}
