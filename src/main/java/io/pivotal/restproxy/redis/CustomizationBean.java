package io.pivotal.restproxy.redis;

import org.springframework.boot.context.embedded.*;
import org.springframework.stereotype.Component;

@Component
public class CustomizationBean implements EmbeddedServletContainerCustomizer {

	public void customize(ConfigurableEmbeddedServletContainer container) {

		container.setPort(8080);

	}

}