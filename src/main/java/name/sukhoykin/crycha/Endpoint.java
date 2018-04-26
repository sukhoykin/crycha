package name.sukhoykin.crycha;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import com.google.gson.Gson;

@ServerEndpoint("/crycha")
public class Endpoint {

    private Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("onOpen");

        // int maxKeySize = 0;
        // try {
        // maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        // } catch (NoSuchAlgorithmException e2) {
        // e2.printStackTrace();
        // }
        // System.out.println("Max Key Size for AES : " + maxKeySize);

        // System.out.println("CustomNamedCurves: ");
        // for (Object name : Collections.list(CustomNamedCurves.getNames())) {
        // System.out.println(name);
        // }
        // System.out.println("ECNamedCurveTable: ");
        // for (Object name : Collections.list(ECNamedCurveTable.getNames())) {
        // System.out.println(name);
        // }
    }

    @OnMessage
    public void onMessage(Session session, String message) {

        System.out.println("onMessage");

        try {

            // Send compressed

            X9ECParameters ecParams = CustomNamedCurves.getByName("curve25519");
            ECParameterSpec ecSpec = new ECParameterSpec(ecParams.getCurve(), ecParams.getG(), ecParams.getN(),
                    ecParams.getH(), ecParams.getSeed());

            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            g.initialize(ecSpec, new SecureRandom());

            KeyPair keyPair = g.generateKeyPair();
            ECPublicKey ecPub = (ECPublicKey) keyPair.getPublic();

            System.out.println("SEND: " + printHexBinary(ecPub.getQ().getEncoded(true)));
            System.out.println(ecPub);

            session.getBasicRemote().sendText(printHexBinary(ecPub.getQ().getEncoded(true)));

            // Derive

            ECPoint ecPoint = ecSpec.getCurve().decodePoint(parseHexBinary(message));
            ECPublicKeySpec ecPubKeySpec = new ECPublicKeySpec(ecPoint, ecSpec);

            KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            ECPublicKey ecPubClient = (ECPublicKey) kf.generatePublic(ecPubKeySpec);

            System.out.println("RECEIVE: " + message);
            System.out.println(ecPubClient);

            KeyAgreement ka = KeyAgreement.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            ka.init(keyPair.getPrivate());
            ka.doPhase(ecPubClient, true);

            byte[] derivedKey = ka.generateSecret();

            System.out.println("derivedKey");
            System.out.println(printHexBinary(derivedKey));

            System.out.println("  server: " + printHexBinary(ecPub.getQ().getEncoded(false)));
            System.out.println("  client: " + printHexBinary(ecPubClient.getQ().getEncoded(false)));

            // Shared secret

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(derivedKey);
            md.update(ecPub.getQ().getEncoded(false));
            md.update(ecPubClient.getQ().getEncoded(false));

            byte[] sharedSecret = md.digest();

            System.out.println("sharedSecret");
            System.out.println(printHexBinary(sharedSecret));

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
