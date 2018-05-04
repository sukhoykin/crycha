package name.sukhoykin.cryptic;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CipherSuite {

    private final byte[] random = new byte[16];
    private final String otpAlgo = "HmacMD5";

    public CipherSuite() {
        new SecureRandom().nextBytes(random);
    }

    public byte[] calculateOtp() throws CipherException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(random, otpAlgo);

        try {

            Mac mac = Mac.getInstance(otpAlgo);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 60) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CipherException(e.getMessage(), e);
        }
    }
}
