package name.sukhoykin.cryptic.command;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.CryptoException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class AuthenticateHandler implements CommandHandler<AuthenticateCommand> {

    private final String PUBLIC_KEY_SIGN_ALGORITHM = "HmacSHA256";

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, AuthenticateCommand command)
            throws CommandException {

        /**
         * Check key signature.
         */
        byte[] totp = client.generateTOTP();

        byte[] dhPub = Hex.decode(command.getDh());
        byte[] dsaPub = Hex.decode(command.getDsa());
        byte[] signature = Hex.decode(command.getSignature());

        if (!Arrays.equals(signature, signPublicKeys(totp, dhPub, dsaPub))) {
            throw new ProtocolException(ClientCloseCode.INVALID_SIGNATURE);
        }

        /**
         * Enable TLS.
         */
        PublicKey dhKey, dsaKey;

        try {

            dhKey = client.decodePublicKey(ClientSession.KEY_EXCHANGE_ALGORITHM, dhPub);
            dsaKey = client.decodePublicKey(ClientSession.SIGNATURE_ALGORITHM, dsaPub);

        } catch (CryptoException e) {
            throw new ProtocolException(ClientCloseCode.INVALID_KEY);
        }

        client.setUpTLS(dhKey, dsaKey, totp);

        /**
         * Send server keys.
         */
        AuthenticateCommand authenticate = new AuthenticateCommand();

        dhPub = client.encodePublicKey(client.getDHKey(), true);
        dsaPub = client.encodePublicKey(client.getDSAKey(), true);
        signature = signPublicKeys(totp, dhPub, dsaPub);

        authenticate.setDh(Hex.toHexString(dhPub));
        authenticate.setDsa(Hex.toHexString(dsaPub));
        authenticate.setSignature(Hex.toHexString(signature));

        client.sendCommand(authenticate);

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
}
