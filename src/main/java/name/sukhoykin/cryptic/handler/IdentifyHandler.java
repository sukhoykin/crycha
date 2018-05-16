package name.sukhoykin.cryptic.handler;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.command.DebugCommand;
import name.sukhoykin.cryptic.command.IdentifyCommand;
import name.sukhoykin.cryptic.exception.CommandException;

public class IdentifyHandler implements CommandHandler<IdentifyCommand> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    private final int TOTP_VALIDITY_MINUTES = 5;
    private final String TOTP_ALGORITHM = "HmacMD5";

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, IdentifyCommand command)
            throws CommandException {

        byte[] totp = calculateTOTP(client.getRandomKey());

        log.debug("EMAIL {} TOTP {}", command.getEmail(), DatatypeConverter.printHexBinary(totp).toLowerCase());

        DebugCommand debug = new DebugCommand("totp");
        debug.setData(DatatypeConverter.printHexBinary(totp).toLowerCase());

        client.setClientId(command.getEmail());
        client.sendCommand(debug);
    }

    public byte[] calculateTOTP(final byte[] randomKey) throws CommandException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(randomKey, TOTP_ALGORITHM);

        try {

            Mac mac = Mac.getInstance(TOTP_ALGORITHM);
            mac.init(secretKeySpec);

            return mac.doFinal(((System.currentTimeMillis() / 1000 / 60 / TOTP_VALIDITY_MINUTES) + "").getBytes());

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CommandException(e);
        }
    }
}
