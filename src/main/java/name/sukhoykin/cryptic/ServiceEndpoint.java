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
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.CloseReason;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.command.AuthenticateHandler;
import name.sukhoykin.cryptic.command.AuthenticateMessage;
import name.sukhoykin.cryptic.command.AuthorizeHandler;
import name.sukhoykin.cryptic.command.AuthorizeMessage;
import name.sukhoykin.cryptic.command.CloseHandler;
import name.sukhoykin.cryptic.command.CloseMessage;
import name.sukhoykin.cryptic.command.DebugMessage;
import name.sukhoykin.cryptic.command.IdentifyHandler;
import name.sukhoykin.cryptic.command.IdentifyMessage;
import name.sukhoykin.cryptic.command.ProhibitHandler;
import name.sukhoykin.cryptic.command.ProhibitMessage;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

@ServerEndpoint(value = "/api", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public final class ServiceEndpoint extends CommandDispatcher implements ServiceSession {

    private static final Logger log = LoggerFactory.getLogger(ServiceEndpoint.class);

    public static final int TOTP_VALIDITY_MINUTES = 5;
    public static final String TOTP_ALGORITHM = "HmacMD5";
    public static final String PUBLIC_KEY_SIGNATURE_ALGORITHM = "HmacSHA256";
    public static final String SHARED_SECRET_HASH_ALGORITHM = "SHA-256";
    public static final String KEY_EXCHANGE_ALGORITHM = "ECDH";
    public static final String SIGNATURE_ALGORITHM = "ECDSA";

    public static final ECParameterSpec CURVE_25519_PARAMETER_SPEC = ECNamedCurveTable.getParameterSpec("curve25519");

    private EndpointConfig config;
    private Session session;
    private String email;

    private final byte[] randomSecret = new byte[16];

    private PublicKey clientDh;
    private PublicKey clientDsa;

    private KeyPair serverDh;
    private KeyPair serverDsa;

    private MessageCipher cipher;
    private MessageSigner signer;

    public ServiceEndpoint() {
        new SecureRandom().nextBytes(randomSecret);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {

        log.debug("#{} Connected", session.getId());

        this.session = session;
        this.config = config;

        addMessageHandler(IdentifyMessage.class, new IdentifyHandler());
    }

    @OnMessage
    public void onMessage(CommandMessage message) {

        try {

            dispatchMessage(this, message);

        } catch (ProtocolException e) {
            close(new CloseReason(e.getCloseCode(), e.getMessage()));

        } catch (CommandException e) {
            close(new CloseReason(CloseCode.SERVER_ERROR, CloseCode.SERVER_ERROR.toString()));
        }
    }

    @Override
    public byte[] identify(String email) throws CommandException {

        if (this.email != null) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_PROTOCOL);
        }

        this.email = email;

        removeMessageHandler(IdentifyMessage.class);
        addMessageHandler(AuthenticateMessage.class, new AuthenticateHandler());

        log.debug("#{} Identified {}", session.getId(), email);

        /** TODO: remove debug message */
        byte[] TOTP = generateTOTP();

        try {
            session.getBasicRemote().sendObject(new DebugMessage(TOTP));
        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }

        return TOTP;
    }

    @Override
    public void authenticate(byte[] dh, byte[] dsa, byte[] signature) throws CommandException {

        if (this.clientDh != null) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_PROTOCOL);
        }

        byte[] TOTP = generateTOTP();

        if (!Arrays.equals(signature, signPublicKeys(dh, dsa, TOTP))) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_SIGNATURE);
        }

        try {

            clientDh = decodePublicKey(ServiceEndpoint.KEY_EXCHANGE_ALGORITHM, dh);
            clientDsa = decodePublicKey(ServiceEndpoint.SIGNATURE_ALGORITHM, dsa);

        } catch (CryptoException e) {
            if (e.getCause() instanceof InvalidKeySpecException) {
                throw new ProtocolException(CloseCode.CLIENT_INVALID_KEY);
            } else {
                throw e;
            }
        }

        serverDh = generateKeyPair(KEY_EXCHANGE_ALGORITHM);
        serverDsa = generateKeyPair(SIGNATURE_ALGORITHM);

        try {

            KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);

            ka.init(serverDh.getPrivate());
            ka.doPhase(clientDh, true);

            byte[] sharedSecret = ka.generateSecret();

            MessageDigest md = MessageDigest.getInstance(SHARED_SECRET_HASH_ALGORITHM);

            byte[] encodeServer = encodePublicKey(serverDh.getPublic(), false);
            byte[] encodeClient = encodePublicKey(clientDh, false);

            md.update(sharedSecret);
            md.update(encodeServer);
            md.update(encodeClient);

            sharedSecret = md.digest();

            cipher = new MessageCipher(sharedSecret, TOTP);
            signer = new MessageSigner(serverDsa.getPrivate(), clientDsa);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new CryptoException(e);
        }

        AuthenticateMessage message = new AuthenticateMessage();

        dh = encodePublicKey(serverDh.getPublic(), true);
        dsa = encodePublicKey(serverDsa.getPublic(), true);
        signature = signPublicKeys(dh, dsa, TOTP);

        message.setDh(dh);
        message.setDsa(dsa);
        message.setSignature(signature);

        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }

        config.getUserProperties().put("cipher", cipher);
        config.getUserProperties().put("signer", signer);

        removeMessageHandler(AuthenticateMessage.class);

        addMessageHandler(AuthorizeMessage.class, new AuthorizeHandler());
        addMessageHandler(ProhibitMessage.class, new ProhibitHandler());
        addMessageHandler(CloseMessage.class, new CloseHandler());

        log.debug("#{} Authenticated", session.getId());
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public PublicKey getClientDh() {
        return clientDh;
    }

    @Override
    public PublicKey getClientDsa() {
        return clientDsa;
    }

    @Override
    public void sendMessage(CommandMessage message) throws CommandException {

        if (cipher == null) {
            throw new ProtocolException(CloseCode.SERVER_INVALID_PROTOCOL);
        }

        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }
    }

    @Override
    public void close(CloseReason reason) {

        try {
            if (session.isOpen()) {
                session.close(reason);
            }
        } catch (IOException e) {
            log.error("Close error", e);
            onClose(reason);
        }
    }

    @OnError
    public void onError(Throwable error) {

        if (error instanceof DecodeException) {

            Throwable cause = error.getCause();
            log.error("#{} {}", session.getId(), error.getMessage());

            if (cause != null) {

                try {
                    throw cause;
                } catch (ProtocolException e) {
                    close(new CloseReason(e.getCloseCode(), error.getMessage()));
                } catch (CryptoException e) {
                    close(new CloseReason(CloseCode.SERVER_ERROR, CloseCode.SERVER_ERROR.toString()));
                } catch (Throwable e) {
                    close(new CloseReason(CloseCode.CLIENT_ERROR, cause.getMessage()));
                }

            } else {
                close(new CloseReason(CloseCode.SERVER_ERROR, CloseCode.SERVER_ERROR.toString()));
            }

        } else {
            log.error("#{} {}", session.getId(), error.getMessage(), error);
            close(new CloseReason(CloseCode.SERVER_ERROR, CloseCode.SERVER_ERROR.toString()));
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {

        CloseMessage message = new CloseMessage();
        message.setEmail(email);

        onMessage(message);

        if (reason.getCloseCode().getCode() < 4000) {
            log.debug("#{} Disconnected", session.getId());
        } else {
            log.error("#{} Disconnected {} {}",
                    new Object[] { session.getId(), reason.getCloseCode().getCode(), reason.getReasonPhrase() });
        }
    }

    private byte[] generateTOTP() throws CryptoException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(randomSecret, TOTP_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 1000 / 60 / TOTP_VALIDITY_MINUTES) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] encodePublicKey(PublicKey key, boolean compressed) {
        return ((ECPublicKey) key).getQ().getEncoded(compressed);
    }

    private PublicKey decodePublicKey(String algorithm, byte[] key) throws CryptoException {

        try {

            KeyFactory kf = KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);

            ECPoint point = ServiceEndpoint.CURVE_25519_PARAMETER_SPEC.getCurve().decodePoint(key);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ServiceEndpoint.CURVE_25519_PARAMETER_SPEC);

            return kf.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] signPublicKeys(byte[] dhPub, byte[] dsaPub, byte[] key) throws CryptoException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, PUBLIC_KEY_SIGNATURE_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(PUBLIC_KEY_SIGNATURE_ALGORITHM);
            mac.init(secretKeySpec);

            mac.update(dhPub);
            mac.update(dsaPub);

            return mac.doFinal();

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CryptoException(e.getMessage(), e);
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
}
