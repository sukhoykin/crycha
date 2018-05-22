package name.sukhoykin.cryptic.command;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;

public class IdentifyCommand implements CommandHandler<IdentifyMessage> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyCommand.class);

    @Override
    public void handleMessage(ServiceDomain service, ClientSession client, IdentifyMessage command)
            throws CommandException {

        byte[] totp = client.generateTOTP();

        log.debug("EMAIL {} TOTP {}", command.getEmail(), DatatypeConverter.printHexBinary(totp).toLowerCase());

        DebugMessage debug = new DebugMessage("totp");
        debug.setData(DatatypeConverter.printHexBinary(totp).toLowerCase());

        client.setEmail(command.getEmail());
        client.sendCommand(debug);
    }
}
