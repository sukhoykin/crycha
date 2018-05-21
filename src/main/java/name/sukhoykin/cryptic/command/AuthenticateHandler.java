package name.sukhoykin.cryptic.command;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientCipher;
import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class AuthenticateHandler implements CommandHandler<AuthenticateCommand> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateHandler.class);

    private final String PUBLIC_KEY_SIGN_ALGORITHM = "HmacSHA256";
    private final String KEY_EXCHANGE_ALGORITHM = "ECDH";
    private final String SIGNATURE_ALGORITHM = "ECDSA";
    private final String SHARED_SECRET_HASH_ALGORITHM = "SHA-256";

    private final ECParameterSpec CURVE_25519_PARAMETER_SPEC = ECNamedCurveTable.getParameterSpec("curve25519");

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, AuthenticateCommand command)
            throws CommandException {

        IdentifyHandler identifyHandler = service.getCommandHandler(IdentifyHandler.class);

        /**
         * Check key signature.
         */
        byte[] totp = identifyHandler.calculateTOTP(client.getRandomKey());
        log.debug("TOTP {}", DatatypeConverter.printHexBinary(totp).toLowerCase());

        byte[] dhPub = Hex.decode(command.getDh());
        byte[] dsaPub = Hex.decode(command.getDsa());
        byte[] signature = Hex.decode(command.getSignature());

        if (!Arrays.equals(signature, signPublicKeys(totp, dhPub, dsaPub))) {
            throw new ProtocolException(ClientCloseCode.INVALID_SIGNATURE);
        }

        PublicKey dhPubKey = decodePublicKey(KEY_EXCHANGE_ALGORITHM, dhPub);
        PublicKey dsaPubKey = decodePublicKey(SIGNATURE_ALGORITHM, dsaPub);

        /**
         * Generate keys and derive shared secret.
         */
        KeyPair dhKeyPair = generateKeyPair(KEY_EXCHANGE_ALGORITHM);
        KeyPair dsaKeyPair = generateKeyPair(SIGNATURE_ALGORITHM);

        byte[] sharedSecret = deriveSharedSecret(dhKeyPair.getPrivate(), dhPubKey);
        sharedSecret = hashSharedSecret(sharedSecret, dhKeyPair.getPublic(), dhPubKey);

        log.debug("sharedSecret: {}", Hex.toHexString(sharedSecret));
        log.debug("iv: {}", Hex.toHexString(totp));

        /**
         * Send server public keys.
         */
        AuthenticateCommand authenticate = new AuthenticateCommand();

        dhPub = encodePublicKey(dhKeyPair.getPublic(), true);
        dsaPub = encodePublicKey(dsaKeyPair.getPublic(), true);
        signature = signPublicKeys(totp, dhPub, dsaPub);

        authenticate.setDh(Hex.toHexString(dhPub));
        authenticate.setDsa(Hex.toHexString(dsaPub));
        authenticate.setSignature(Hex.toHexString(signature));

        client.sendCommand(authenticate);

        /**
         * Enable TLS.
         */
        client.setServerDSAKey(dsaKeyPair.getPrivate());
        client.setClientDSAKey(dsaPubKey);
        client.setCipher(new ClientCipher(sharedSecret, totp));

        service.registerClient(client);
    }

    private byte[] signPublicKeys(byte[] key, byte[] dhPub, byte[] dsaPub) throws CryptoException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, PUBLIC_KEY_SIGN_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(PUBLIC_KEY_SIGN_ALGORITHM);
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

            ECPoint point = CURVE_25519_PARAMETER_SPEC.getCurve().decodePoint(key);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, CURVE_25519_PARAMETER_SPEC);

            return kf.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
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

    private byte[] deriveSharedSecret(PrivateKey serverPrivateKey, PublicKey clientPublicKey) throws CryptoException {

        try {

            KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);

            ka.init(serverPrivateKey);
            ka.doPhase(clientPublicKey, true);

            return ka.generateSecret();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
    }

    private byte[] encodePublicKey(PublicKey key, boolean compressed) {
        return ((ECPublicKey) key).getQ().getEncoded(compressed);
    }

    private byte[] hashSharedSecret(byte[] sharedSecret, PublicKey serverPublicKey, PublicKey clientPublicKey)
            throws CryptoException {

        try {

            MessageDigest md = MessageDigest.getInstance(SHARED_SECRET_HASH_ALGORITHM);

            md.update(sharedSecret);
            md.update(encodePublicKey(serverPublicKey, false));
            md.update(encodePublicKey(clientPublicKey, false));

            return md.digest();

        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }
}
