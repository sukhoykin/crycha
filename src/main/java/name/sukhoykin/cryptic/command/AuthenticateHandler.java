package name.sukhoykin.cryptic.command;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import name.sukhoykin.cryptic.CloseCode;
import name.sukhoykin.cryptic.CloseReason;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServerSession;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class AuthenticateHandler extends CommandHandler<AuthenticateMessage> {

    private static final String PUBLIC_KEY_SIGNATURE_ALGORITHM = "HmacSHA256";

    public AuthenticateHandler(ServerSession session) {
        super(session);
    }

    @Override
    public void onMessage(ServerSession session, AuthenticateMessage message) throws CommandException {

        byte[] dh = message.getDh();
        byte[] dsa = message.getDsa();
        byte[] signature = message.getSignature();

        byte[] TOTP = session.generateTOTP();

        if (!Arrays.equals(signature, signPublicKeys(dh, dsa, TOTP))) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_SIGNATURE);
        }

        try {

            PublicKey clientDh = decodePublicKey(ServerSession.KEY_EXCHANGE_ALGORITHM, dh);
            PublicKey clientDsa = decodePublicKey(ServerSession.SIGNATURE_ALGORITHM, dsa);

            session.establishSecurity(clientDh, clientDsa, TOTP);

        } catch (CryptoException e) {
            throw new ProtocolException(CloseCode.CLIENT_INVALID_KEY);
        }

        AuthenticateMessage authenticate = new AuthenticateMessage();

        dh =  encodePublicKey(session.getServerDh());
        dsa = encodePublicKey(session.getServerDsa());
        signature = signPublicKeys(dh, dsa, TOTP);

        authenticate.setDh(dh);
        authenticate.setDsa(dsa);
        authenticate.setSignature(signature);

        session.sendMessage(authenticate);

        session = getSessions().put(session.getEmail(), session);

        if (session != null) {
            session.close(new CloseReason(CloseCode.DUPLICATE_AUTHENTICATION));
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

    private PublicKey decodePublicKey(String algorithm, byte[] key) throws CryptoException {

        try {

            KeyFactory kf = KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);

            ECPoint point = ServerSession.CURVE_25519_PARAMETER_SPEC.getCurve().decodePoint(key);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ServerSession.CURVE_25519_PARAMETER_SPEC);

            return kf.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] encodePublicKey(PublicKey key) {
        return ((ECPublicKey) key).getQ().getEncoded(true);
    }
}
