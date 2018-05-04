package name.sukhoykin.cryptic.command;

public class IdentifyCommand extends CommandMessage {

    private String email;

    public IdentifyCommand() {
        super("identify");
    }

    public String getEmail() {
        return email;
    }
}
