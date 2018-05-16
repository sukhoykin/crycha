package name.sukhoykin.cryptic.handler;

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

import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandException;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ProtocolException;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.command.AuthenticateCommand;

public class AuthenticateHandler implements CommandHandler<AuthenticateCommand> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateHandler.class);

    private final String PUBLIC_KEY_SIGN_ALGO = "HmacSHA256";
    private final String KEY_EXCHANGE_ALGO = "ECDH";
    private final String SIGNATURE_ALGO = "ECDSA";
    private final String SHARED_SECRET_HASH_ALGO = "SHA-256";

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

        PublicKey dhPubKey = decodePublicKey(KEY_EXCHANGE_ALGO, dhPub);
        PublicKey dsaPubKey = decodePublicKey(SIGNATURE_ALGO, dsaPub);

        /**
         * Generate keys and derive shared secret.
         */
        KeyPair dhKeyPair = generateKeyPair(KEY_EXCHANGE_ALGO);
        KeyPair dsaKeyPair = generateKeyPair(SIGNATURE_ALGO);

        byte[] sharedSecret = deriveSharedSecret(dhKeyPair.getPrivate(), dhPubKey);
        sharedSecret = hashSharedSecret(sharedSecret, dhKeyPair.getPublic(), dhPubKey);

        log.debug("sharedSecret: {}", Hex.toHexString(sharedSecret));

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
        service.registerClient(client);
    }

    private byte[] signPublicKeys(byte[] key, byte[] dhPub, byte[] dsaPub) throws CommandException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, PUBLIC_KEY_SIGN_ALGO);

        try {

            Mac mac = Mac.getInstance(PUBLIC_KEY_SIGN_ALGO);
            mac.init(secretKeySpec);

            mac.update(dhPub);
            mac.update(dsaPub);

            return mac.doFinal();

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    private PublicKey decodePublicKey(String algorithm, byte[] key) throws CommandException {

        try {

            KeyFactory kf = KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);

            ECPoint point = CURVE_25519_PARAMETER_SPEC.getCurve().decodePoint(key);
            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, CURVE_25519_PARAMETER_SPEC);

            return kf.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new CommandException(e);
        }
    }

    private KeyPair generateKeyPair(String algorithm) throws CommandException {

        try {

            KeyPairGenerator g = KeyPairGenerator.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
            g.initialize(CURVE_25519_PARAMETER_SPEC, new SecureRandom());

            return g.generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new CommandException(e);
        }
    }

    private byte[] deriveSharedSecret(PrivateKey serverPrivateKey, PublicKey clientPublicKey) throws CommandException {

        try {

            KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGO, BouncyCastleProvider.PROVIDER_NAME);

            ka.init(serverPrivateKey);
            ka.doPhase(clientPublicKey, true);

            return ka.generateSecret();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            throw new CommandException(e);
        }
    }

    private byte[] encodePublicKey(PublicKey key, boolean compressed) {
        return ((ECPublicKey) key).getQ().getEncoded(compressed);
    }

    private byte[] hashSharedSecret(byte[] sharedSecret, PublicKey serverPublicKey, PublicKey clientPublicKey)
            throws CommandException {

        try {

            MessageDigest md = MessageDigest.getInstance(SHARED_SECRET_HASH_ALGO);

            md.update(sharedSecret);
            md.update(encodePublicKey(serverPublicKey, false));
            md.update(encodePublicKey(clientPublicKey, false));

            return md.digest();

        } catch (NoSuchAlgorithmException e) {
            throw new CommandException(e);
        }
    }
}
