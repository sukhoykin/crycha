package name.sukhoykin.cryptic.command;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

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

public class EnvelopeHandler implements CommandHandler<EnvelopeCommand> {

    private static final Logger log = LoggerFactory.getLogger(EnvelopeHandler.class);
    
    private final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, EnvelopeCommand command)
            throws CommandException {

//        ClientCipher cipher = client.getCipher();
//        
//        byte[] payload = Hex.decode(command.getPayload());
//        byte[] signature = Hex.decode(command.getSignature());
//        
//        if (!verifySignature(client.getClientDSAKey(), payload, signature)) {
//            throw new ProtocolException(ClientCloseCode.INVALID_SIGNATURE);
//        }
//        
//        payload = cipher.decrypt(payload);
        
//        byte[] payload = cipher.decrypt(Hex.decode(command.getPayload()));
//        log.debug(Arrays.toString(output));
//        String payload = new String(output);
//        log.debug("Payload: {}", payload);
//
//        AuthorizeCommand authorize = new AuthorizeCommand();
//        authorize.setEmail("d@example.com");
//
//        client.sendCommand(authorize);
    }
    
    private boolean verifySignature(PublicKey key, byte[] payload, byte[] signature) throws CryptoException {
        
        try {
            
            Signature s = Signature.getInstance(SIGNATURE_ALGORITHM);
            
            s.initVerify(key);
            s.update(payload);
            
            return s.verify(signature);
            
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new CryptoException(e);
        }
    }
}
