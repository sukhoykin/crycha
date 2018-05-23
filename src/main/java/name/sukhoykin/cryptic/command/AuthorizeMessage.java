package name.sukhoykin.cryptic.command;

import name.sukhoykin.cryptic.CommandMessage;

public class AuthorizeMessage extends CommandMessage {

    public static final String NAME = "authorize";

    private String email;
    private String dh;
    private String dsa;

    public AuthorizeMessage() {
        super(NAME);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
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
}
