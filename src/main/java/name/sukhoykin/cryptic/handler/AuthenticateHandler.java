package name.sukhoykin.cryptic.handler;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CipherException;
import name.sukhoykin.cryptic.ClientEndpoint;
import name.sukhoykin.cryptic.ClientService;
import name.sukhoykin.cryptic.command.AuthenticateCommand;

public class AuthenticateHandler implements CommandHandler<AuthenticateCommand> {
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticateHandler.class);

    @Override
    public void handleCommand(ClientService service, AuthenticateCommand command) throws CommandException {
        
        ClientEndpoint client = service.getClientEndpoint();

        try {
            
            byte[] otp = client.getCipherSuite().calculateOtp();
            log.debug("OTP {}", DatatypeConverter.printHexBinary(otp).toLowerCase());
            
            byte[] dhPub = DatatypeConverter.parseHexBinary(command.getDh());
            byte[] dsaPub = DatatypeConverter.parseHexBinary(command.getDsa());
            
            byte[] signature = client.getCipherSuite().signPublicKeys(otp, dhPub, dsaPub);
            
            log.debug("{} = {}", command.getSignature(), DatatypeConverter.printHexBinary(signature).toLowerCase());
            
        } catch (CipherException e) {
            throw new CommandException(e);
        }
    }
}
