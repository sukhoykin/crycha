package name.sukhoykin.cryptic.command;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.ClientCloseCode;
import name.sukhoykin.cryptic.ClientSession;
import name.sukhoykin.cryptic.CommandHandler;
import name.sukhoykin.cryptic.ServiceDomain;
import name.sukhoykin.cryptic.exception.CommandException;
import name.sukhoykin.cryptic.exception.ProtocolException;

public class IdentifyCommand implements CommandHandler<IdentifyMessage> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyCommand.class);

    @Override
    public void handleMessage(ServiceDomain service, ClientSession client, IdentifyMessage message)
            throws CommandException {

        if (client.getEmail() != null) {
            throw new ProtocolException(ClientCloseCode.CLIENT_ERROR);
        }

        byte[] totp = client.generateTOTP();

        log.debug("EMAIL {} TOTP {}", message.getEmail(), DatatypeConverter.printHexBinary(totp).toLowerCase());

        DebugMessage debug = new DebugMessage();
        debug.setData(DatatypeConverter.printHexBinary(totp).toLowerCase());

        client.setEmail(message.getEmail());
        client.sendMessage(debug);
    }
}
