package name.sukhoykin.cryptic.handler;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, AuthenticateCommand command)
            throws CommandException {

        IdentifyHandler identifyHandler = service.getCommandHandler(IdentifyHandler.class);

        byte[] totp = identifyHandler.calculateTOTP(client.getRandomKey());
        log.debug("TOTP {}", DatatypeConverter.printHexBinary(totp).toLowerCase());

        byte[] dhPub = DatatypeConverter.parseHexBinary(command.getDh());
        byte[] dsaPub = DatatypeConverter.parseHexBinary(command.getDsa());
        byte[] signature = DatatypeConverter.parseHexBinary(command.getSignature());

        if (!Arrays.equals(signature, signPublicKeys(totp, dhPub, dsaPub))) {
            throw new ProtocolException(ClientCloseCode.INVALID_SIGNATURE);
        }

        service.registerClient(client);
    }

    public byte[] signPublicKeys(byte[] key, byte[] dhPub, byte[] dsaPub) throws CommandException {

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
}
