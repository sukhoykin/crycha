package name.sukhoykin.cryptic;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

@ServerEndpoint(value = "/api", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public final class ServerSession {

    private static final Logger log = LoggerFactory.getLogger(ServerSession.class);

    public static final int TOTP_VALIDITY_MINUTES = 5;
    public static final String TOTP_ALGORITHM = "HmacMD5";
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

    public ServerSession() {
        new SecureRandom().nextBytes(randomSecret);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public PublicKey getClientDh() {
        return clientDh;
    }

    public PublicKey getClientDsa() {
        return clientDsa;
    }

    public PublicKey getServerDh() {
        return serverDh != null ? serverDh.getPublic() : null;
    }

    public PublicKey getServerDsa() {
        return serverDsa != null ? serverDsa.getPublic() : null;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {

        log.debug("#{} Connected", session.getId());

        // TODO: check empty handlers

        this.session = session;
        this.config = config;
    }

    @OnError
    public void onError(Throwable error) {

        if (error instanceof DecodeException) {

            log.error("#{} {}", session.getId(), error.getMessage());

            Throwable cause = error.getCause();

            if (cause != null) {

                try {
                    throw cause;

                } catch (ProtocolException e) {
                    close(new CloseReason(e.getCloseCode()));

                } catch (CryptoException e) {
                    close(new CloseReason(CloseCode.SERVER_ERROR));

                } catch (Throwable e) {
                    close(new CloseReason(CloseCode.CLIENT_ERROR, error.getMessage()));
                }

            } else {
                close(new CloseReason(CloseCode.SERVER_ERROR));
            }

        } else {
            log.error("#{} {}", session.getId(), error.getMessage(), error);
            close(new CloseReason(CloseCode.SERVER_ERROR));
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {

        if (reason.getCloseCode().getCode() < 4000) {
            log.debug("#{} Disconnected", session.getId());
        } else {
            log.error("#{} Disconnected {} {}",
                    new Object[] { session.getId(), reason.getCloseCode().getCode(), reason.getReasonPhrase() });
        }
    }

    public byte[] generateTOTP() throws CryptoException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(randomSecret, TOTP_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 1000 / 60 / TOTP_VALIDITY_MINUTES) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    public void establishSecurity(PublicKey clientDh, PublicKey clientDsa, byte[] IV) throws CommandException {

        this.clientDh = clientDh;
        this.clientDsa = clientDsa;

        serverDh = generateKeyPair(KEY_EXCHANGE_ALGORITHM);
        serverDsa = generateKeyPair(SIGNATURE_ALGORITHM);

        try {

            KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);

            ka.init(serverDh.getPrivate());
            ka.doPhase(clientDh, true);

            byte[] sharedSecret = ka.generateSecret();

            MessageDigest md = MessageDigest.getInstance(SHARED_SECRET_HASH_ALGORITHM);

            byte[] encodeServer = ((ECPublicKey) serverDh.getPublic()).getQ().getEncoded(false);
            byte[] encodeClient = ((ECPublicKey) clientDh).getQ().getEncoded(false);

            md.update(sharedSecret);
            md.update(encodeServer);
            md.update(encodeClient);

            sharedSecret = md.digest();

            cipher = new MessageCipher(sharedSecret, IV);
            signer = new MessageSigner(serverDsa.getPrivate(), clientDsa);

            config.getUserProperties().put("cipher", cipher);
            config.getUserProperties().put("signer", signer);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    public void sendMessage(CommandMessage message) throws CommandException {

        try {

            session.getBasicRemote().sendObject(message);
            log.debug("#{} SEND {}", session.getId(), message);

        } catch (IOException | EncodeException e) {
            throw new CommandException(e);
        }
    }

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
