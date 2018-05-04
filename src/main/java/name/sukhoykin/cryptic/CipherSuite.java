package name.sukhoykin.cryptic;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CipherSuite {
    
    private static final Logger log = LoggerFactory.getLogger(CipherSuite.class);

    private final byte[] random = new byte[16];

    private final String TOTP_ALGO = "HmacMD5";
    private final String PUBLIC_KEY_SIGN_ALGO = "HmacSHA256";

    public CipherSuite() {
        new SecureRandom().nextBytes(random);
    }

    public byte[] calculateOtp() throws CipherException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(random, TOTP_ALGO);

        try {

            Mac mac = Mac.getInstance(TOTP_ALGO);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 1000 / 60) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }

    public byte[] signPublicKeys(byte[] key, byte[] dhPub, byte[] dsaPub) throws CipherException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, PUBLIC_KEY_SIGN_ALGO);

        try {

            Mac mac = Mac.getInstance(PUBLIC_KEY_SIGN_ALGO);
            mac.init(secretKeySpec);
            
            log.debug("{}", dhPub);
            log.debug("{}", dsaPub);

            mac.update(dhPub);
            mac.update(dsaPub);

            return mac.doFinal();

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }
}
