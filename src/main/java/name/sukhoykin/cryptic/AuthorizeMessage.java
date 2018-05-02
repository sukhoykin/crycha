package name.sukhoykin.cryptic;

public class AuthorizeMessage {

	private final String command = "AUTHORIZE";
	private String dhpub;
	
	public void setDhPub(String dhpub) {
		this.dhpub = dhpub;
	}
	
	public String getDhPub() {
		return dhpub;
	}
}
