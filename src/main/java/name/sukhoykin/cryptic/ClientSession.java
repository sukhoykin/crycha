package name.sukhoykin.cryptic;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.command.AuthenticateMessage;
import name.sukhoykin.cryptic.command.DebugMessage;
import name.sukhoykin.cryptic.command.EnvelopeMessage;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;

public class ClientSession {

    private final static Logger log = LoggerFactory.getLogger(ClientSession.class);

    public static final int TOTP_VALIDITY_MINUTES = 5;
    public static final String TOTP_ALGORITHM = "HmacMD5";
    public static final String SHARED_SECRET_HASH_ALGORITHM = "SHA-256";
    public static final String KEY_EXCHANGE_ALGORITHM = "ECDH";
    public static final String SIGNATURE_ALGORITHM = "ECDSA";
    public static final String SIGNATURE_HASH_ALGORITHM = "SHA256withECDSA";

    private static final ECParameterSpec CURVE_25519_PARAMETER_SPEC = ECNamedCurveTable.getParameterSpec("curve25519");

    private final Session session;
    private String email;

    private final byte[] randomSecret = new byte[16];

    private PublicKey dhKey;
    private PublicKey dsaKey;

    private KeyPair dhKeyPair;
    private KeyPair dsaKeyPair;

    private ClientCipher cipher;

    public ClientSession(Session session) {
        this.session = session;
        new SecureRandom().nextBytes(randomSecret);
    }

    Session getSession() {
        return session;
    }

    public void setEmail(String email) {

        if (this.email != null) {
            throw new IllegalStateException("Client already identified with email: " + this.email);
        }

        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public PublicKey getClientDHKey() {

        if (dhKey == null) {
            throw new IllegalStateException("TLS is not enabled");
        }

        return dhKey;
    }

    public PublicKey getClientDSAKey() {

        if (dsaKey == null) {
            throw new IllegalStateException("TLS is not enabled");
        }

        return dsaKey;
    }

    public PublicKey getDHKey() {

        if (dhKeyPair == null) {
            throw new IllegalStateException("TLS is not enabled");
        }

        return dhKeyPair.getPublic();
    }

    public PublicKey getDSAKey() {

        if (dsaKeyPair == null) {
            throw new IllegalStateException("TLS is not enabled");
        }

        return dsaKeyPair.getPublic();
    }

    public byte[] generateTOTP() throws CommandException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(randomSecret, TOTP_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 1000 / 60 / TOTP_VALIDITY_MINUTES) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CommandException(e);
        }
    }

    public PublicKey decodePublicKey(String algorithm, byte[] key) throws CryptoException {

        try {

            KeyFactory kf = KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);

            ECPoint point = CURVE_25519_PARAMETER_SPEC.getCurve().decodePoint(key);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, CURVE_25519_PARAMETER_SPEC);

            return kf.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] encodePublicKey(PublicKey key, boolean compressed) {
        return ((ECPublicKey) key).getQ().getEncoded(compressed);
    }

    public void setUpTLS(PublicKey dhKey, PublicKey dsaKey, byte[] iv) throws CryptoException {

        this.dhKey = dhKey;
        this.dsaKey = dsaKey;

        dhKeyPair = generateKeyPair(KEY_EXCHANGE_ALGORITHM);
        dsaKeyPair = generateKeyPair(SIGNATURE_ALGORITHM);

        try {

            KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);

            ka.init(dhKeyPair.getPrivate());
            ka.doPhase(dhKey, true);

            byte[] sharedSecret = ka.generateSecret();

            MessageDigest md = MessageDigest.getInstance(SHARED_SECRET_HASH_ALGORITHM);

            md.update(sharedSecret);
            md.update(encodePublicKey(dhKeyPair.getPublic(), false));
            md.update(encodePublicKey(dhKey, false));

            sharedSecret = md.digest();

            cipher = new ClientCipher(sharedSecret, iv);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    private KeyPair generateKeyPair(String algorithm) throws CryptoException {

        try {

            KeyPairGenerator g = KeyPairGenerator.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
            g.initialize(CURVE_25519_PARAMETER_SPEC, new SecureRandom());

            return g.generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }

    public boolean verifyPayload(byte[] payload, byte[] signature) throws CryptoException {

        try {

            Signature s = Signature.getInstance(SIGNATURE_HASH_ALGORITHM);

            s.initVerify(dsaKey);
            s.update(payload);

            return s.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] decryptPayload(byte[] payload) throws CryptoException {
        return cipher.decrypt(payload);
    }

    public void sendMessage(CommandMessage message) throws CommandException {

        if (!(message instanceof AuthenticateMessage) && !(message instanceof DebugMessage)) {

            if (cipher == null) {
                throw new IllegalStateException("TLS must be enabled for " + message.getClass().getName() + " command");
            }

            byte[] payload = cipher.encrypt(message.toString().getBytes());
            byte[] signature = signPayload(payload);

            EnvelopeMessage envelope = new EnvelopeMessage();
            envelope.setPayload(Hex.toHexString(payload));
            envelope.setSignature(Hex.toHexString(signature));

            message = envelope;
        }

        try {

            session.getBasicRemote().sendObject(message);

            if (log.isDebugEnabled()) {
                log.debug("#{} SEND {}", session.getId(), message);
            }

        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }
    }

    private byte[] signPayload(byte[] payload) throws CryptoException {

        try {

            Signature s = Signature.getInstance(SIGNATURE_HASH_ALGORITHM);

            s.initSign(dsaKeyPair.getPrivate());
            s.update(payload);

            return s.sign();

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptoException(e);
        }
    }
}
