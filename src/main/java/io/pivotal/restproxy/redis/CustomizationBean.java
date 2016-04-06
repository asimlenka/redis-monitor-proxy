package io.pivotal.restproxy.redis;

import org.springframework.boot.context.embedded.*;
import org.springframework.stereotype.Component;


@Component
public class CustomizationBean implements EmbeddedServletContainerCustomizer {

	
	public void customize(ConfigurableEmbeddedServletContainer container) {
		// JettyEmbeddedServletContainerFactory
		

		container.setPort(8080);

//		try {
//			InetAddress i = InetAddress.getByName(host);
//			container.setAddress(i);
//			// String docRoot = SystemMonitor.getInstance().getMonitorHome() +
//			// "/skins/blkNavBar";
//			// String docRoot = SystemMonitor.getInstance().getMonitorHome();
//			String docRoot = SystemMonitor.getInstance().getConfig().getUI()
//					.getSkinFolder();
//
//			System.out.println("Doc root is: " + docRoot);
//			System.out.println("Type is: " + container.getClass().getName());
//			container.setDocumentRoot(new File(docRoot));
//			// see if we can tweak it
//			JettyEmbeddedServletContainerFactory jc = (JettyEmbeddedServletContainerFactory) container;
//			System.out.println("Default Servlet: "
//					+ jc.isRegisterDefaultServlet());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}