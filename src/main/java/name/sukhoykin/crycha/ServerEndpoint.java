package name.sukhoykin.crycha;

import java.io.IOException;
import java.security.spec.ECGenParameterSpec;
import java.util.Collections;
import java.util.Random;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.ECNamedCurveTable;

import com.google.gson.Gson;

@ServerEndpoint("/crycha")
public class ServerEndpoint {
	
	private Gson gson = new Gson();
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("onOpen");
		
		System.out.println("CustomNamedCurves: ");
		for (Object name : Collections.list(CustomNamedCurves.getNames())) {
			System.out.println(name);	
		}
		System.out.println("ECNamedCurveTable: ");
		for (Object name : Collections.list(ECNamedCurveTable.getNames())) {
			System.out.println(name);	
		}
	}
	
	@OnMessage
	public void onMessage(Session session, String message) {
		
		System.out.println("onMessage " + message);
		
		AuthorizeMessage authorize = new AuthorizeMessage();
		authorize.setDhPub(new Random().nextDouble() + "");
		
		
		
		try {
			
			ECGenParameterSpec ecGenSpec = 
			
			
			
			X9ECParameters x9ecp = CustomNamedCurves.getByName("curve25519");
			
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
			kpg.initialize(new ECGenParameterSpec("Curve25519"));
			
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		try {
			session.getBasicRemote().sendText(gson.toJson(authorize));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnClose
	public void onClose(Session session) {
		System.out.println("onClose");
	}
	
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("onError " + error.getMessage());
	}
}
