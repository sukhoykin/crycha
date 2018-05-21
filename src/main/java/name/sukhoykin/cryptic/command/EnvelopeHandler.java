package name.sukhoykin.cryptic.command;

import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientCipher;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;

public class EnvelopeHandler implements CommandHandler<EnvelopeCommand> {

    private static final Logger log = LoggerFactory.getLogger(EnvelopeHandler.class);

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, EnvelopeCommand command)
            throws CommandException {

        ClientCipher cipher = client.getCipher();

        log.debug(command.getPayload());
        log.debug(Arrays.toString(Hex.decode(command.getPayload())));

        byte[] output = cipher.decrypt(Hex.decode(command.getPayload()));
        log.debug(Arrays.toString(output));
        String payload = new String(output);
        log.debug("Payload: {}", payload);

        command = new EnvelopeCommand();

        AuthorizeCommand authorize = new AuthorizeCommand();
        authorize.setEmail("d@example.com");

        command.setPayload(Hex.toHexString(cipher.encrypt(authorize.toString().getBytes())));

        client.sendCommand(command);
    }
}
