package name.sukhoykin.cryptic.command;

public class AuthenticateCommand extends CommandMessage {

    private String dh;
    private String dsa;
    private String signature;

    public AuthenticateCommand() {
        super("authenticate");
    }

    public void setDh(String dh) {
        this.dh = dh;
    }

    public String getDh() {
        return dh;
    }

    public void setDsa(String dsa) {
        this.dsa = dsa;
    }

    public String getDsa() {
        return dsa;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
