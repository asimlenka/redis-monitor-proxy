package io.pivotal.restproxy.redis;

import io.pivotal.restproxy.redis.services.APIService;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

	static Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		log.info("Starting redis-api");
//		SpringApplication app = new SpringApplication(Application.class, args);
		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		// Register the Spring Context with the APIService
		APIService svc = ctx.getBean(APIService.class);
		svc.setCtx(ctx);
		
		
		log.debug("Let's inspect the beans provided by Spring Boot:");

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			log.debug(beanName);
		}
	}
}