package name.sukhoykin.cryptic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.sukhoykin.cryptic.CipherSuite;
import name.sukhoykin.cryptic.ClientService;
import name.sukhoykin.cryptic.command.IdentifyCommand;

public class IdentifyHandler implements CommandHandler<IdentifyCommand> {
    
    private static final Logger log = LoggerFactory.getLogger(IdentifyHandler.class);

    @Override
    public void handleCommand(ClientService service, IdentifyCommand command) throws CommandException {

        log.debug("Identify: {}", command.getEmail());
        
        CipherSuite suite = new CipherSuite();

    }
}
