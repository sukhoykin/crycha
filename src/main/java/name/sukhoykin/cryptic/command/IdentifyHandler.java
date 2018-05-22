package name.sukhoykin.cryptic.command;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;

public class IdentifyHandler implements CommandHandler<IdentifyCommand> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void handleCommand(ServiceDomain service, ClientSession client, IdentifyCommand command)
            throws CommandException {

        byte[] totp = client.generateTOTP();

        log.debug("EMAIL {} TOTP {}", command.getEmail(), DatatypeConverter.printHexBinary(totp).toLowerCase());

        DebugCommand debug = new DebugCommand("totp");
        debug.setData(DatatypeConverter.printHexBinary(totp).toLowerCase());

        client.setEmail(command.getEmail());
        client.sendCommand(debug);
    }
}
