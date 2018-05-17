package name.sukhoykin.cryptic;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import name.sukhoykin.cryptic.exception.CryptoException;

public class ClientCipher {

    private final String CIPHER_ALGORITHM = "AES";
    private final String CIPHER_MODE = "AES/CBC/PKCS7PADDING";

    private SecretKeySpec secretKey;
    private Cipher encrypt, decrypt;
    private byte[] encryptIV = new byte[16];
    private byte[] decryptIV = new byte[16];

    public ClientCipher(byte[] sharedSecret, byte[] iv) throws CryptoException {

        secretKey = new SecretKeySpec(sharedSecret, CIPHER_ALGORITHM);

        try {

            encrypt = Cipher.getInstance(CIPHER_MODE, BouncyCastleProvider.PROVIDER_NAME);
            decrypt = Cipher.getInstance(CIPHER_MODE, BouncyCastleProvider.PROVIDER_NAME);

            encrypt.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            decrypt.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] encrypt(byte[] input) throws CryptoException {

        try {

            byte[] output = encrypt.doFinal(input);

            System.arraycopy(output, output.length - 16, encryptIV, 0, 16);
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(encryptIV));

            return output;

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }

    public byte[] decrypt(byte[] input) throws CryptoException {

        try {

            byte[] output = decrypt.doFinal(input);

            System.arraycopy(input, input.length - 16, decryptIV, 0, 16);
            decrypt.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(decryptIV));

            return output;

        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }
}
