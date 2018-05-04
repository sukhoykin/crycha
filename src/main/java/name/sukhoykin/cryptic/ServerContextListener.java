package name.sukhoykin.cryptic;

import java.security.Security;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

@WebListener
public class ServerContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }
}
