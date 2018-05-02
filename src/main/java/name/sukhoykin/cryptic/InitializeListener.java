package name.sukhoykin.cryptic;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

@WebListener
public class InitializeListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("init");
		Provider p = new BouncyCastleProvider();
		Security.addProvider(p);
		
		//for (Provider p : Security.getProviders()) {
		//	System.out.println("Provider: " + p.getName() + ": " + p.getInfo());
//			for (Service s : p.getServices()) {
//				System.out.println("  " + s.getType() + ": " + s.getAlgorithm());
//			}
		//}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("destroy");
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
	}
}