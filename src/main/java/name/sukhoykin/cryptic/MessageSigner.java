package name.sukhoykin.cryptic;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import name.sukhoykin.cryptic.exception.CryptoException;

public class MessageSigner {

    private static final String SIGNATURE_HASH_ALGORITHM = "SHA256withECDSA";

    private final PrivateKey serverDsa;
    private final PublicKey clientDsa;

    public MessageSigner(PrivateKey serverDsa, PublicKey clientDsa) {
        this.serverDsa = serverDsa;
        this.clientDsa = clientDsa;
    }

    public byte[] sign(byte[] payload) throws CryptoException {

        try {

            Signature s = Signature.getInstance(SIGNATURE_HASH_ALGORITHM);

            s.initSign(serverDsa);
            s.update(payload);

            return s.sign();

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptoException(e);
        }
    }

    public boolean verify(byte[] payload, byte[] signature) throws CryptoException {

        try {

            Signature s = Signature.getInstance(SIGNATURE_HASH_ALGORITHM);

            s.initVerify(clientDsa);
            s.update(payload);

            return s.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptoException(e);
        }
    }
}
