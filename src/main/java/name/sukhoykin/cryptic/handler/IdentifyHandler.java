package name.sukhoykin.cryptic.handler;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CipherException;
import name.sukhoykin.cryptic.ClientEndpoint;
import name.sukhoykin.cryptic.ClientService;
import name.sukhoykin.cryptic.command.IdentifyCommand;

public class IdentifyHandler implements CommandHandler<IdentifyCommand> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void handleCommand(ClientService service, IdentifyCommand command) throws CommandException {

        ClientEndpoint client = service.getClientEndpoint();
        client.setId(command.getEmail());

        try {

            byte[] otp = client.getCipherSuite().calculateOtp();

            log.debug("EMAIL {} {}", command.getEmail(), DatatypeConverter.printHexBinary(otp).toLowerCase());

        } catch (CipherException e) {
            throw new CommandException(e);
        }
    }
}
