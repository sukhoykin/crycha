package name.sukhoykin.cryptic.command;

public class AuthenticateCommand extends CommandMessage {

    private String dh;
    private String dsa;
    private String signature;

    public AuthenticateCommand() {
        super("authenticate");
    }

    public String getDh() {
        return dh;
    }

    public String getDsa() {
        return dsa;
    }

    public String getSignature() {
        return signature;
    }
}
