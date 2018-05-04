package name.sukhoykin.cryptic.handler;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CipherException;
import name.sukhoykin.cryptic.ClientEndpoint;
import name.sukhoykin.cryptic.ClientService;
import name.sukhoykin.cryptic.command.DebugCommand;
import name.sukhoykin.cryptic.command.IdentifyCommand;

public class IdentifyHandler implements CommandHandler<IdentifyCommand> {

    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void handleCommand(ClientService service, IdentifyCommand command) throws CommandException {

        ClientEndpoint client = service.getClientEndpoint();
        client.setId(command.getEmail());

        try {

            byte[] otp = client.getCipherSuite().calculateOtp();

            log.debug("EMAIL {} OTP {}", command.getEmail(), DatatypeConverter.printHexBinary(otp).toLowerCase());

            DebugCommand debug = new DebugCommand("totp");
            debug.setData(DatatypeConverter.printHexBinary(otp).toLowerCase());

            client.sendCommand(debug);

        } catch (CipherException | IOException | EncodeException e) {
            throw new CommandException(e);
        }
    }
}
