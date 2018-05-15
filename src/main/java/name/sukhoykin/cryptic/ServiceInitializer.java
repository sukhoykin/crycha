package name.sukhoykin.cryptic;

import java.security.Security;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

@WebListener
public class ServiceInitializer extends Configurator implements ServletContextListener {

    private static final ServiceEndpoint SERVICE = new ServiceEndpoint();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {

        if (endpointClass.isInstance(SERVICE)) {
            return (T) SERVICE;
        }

        throw new InstantiationException("Unsupported endpoint class: " + endpointClass.getName());
    }
}
