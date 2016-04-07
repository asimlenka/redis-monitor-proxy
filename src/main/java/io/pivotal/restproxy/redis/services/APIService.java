package io.pivotal.restproxy.redis.services;

import io.pivotal.restproxy.redis.messages.Credentials;
import io.pivotal.restproxy.redis.messages.GenericResponse;
import io.pivotal.restproxy.redis.messages.RedisConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;

@Service
public class APIService {

	// A reference to the Spring Application context so we can dynamically bind
	// to beans instead
	// of using dependency injection

	private ApplicationContext ctx;

	private Logger log = LoggerFactory.getLogger(APIService.class);

	public ApplicationContext getCtx() {
		return ctx;
	}

	public void setCtx(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	public GenericResponse dump() {

		log.info("Gathering Dump Data");

		String result = "";
		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");

		RedisConfig redisConfig = null;

		result += "************************** Environment Vars ***************\n";
		Map<String, String> envs = System.getenv();
		for (String key : envs.keySet()) {
			result += key + " = " + envs.get(key) + "\n";
		}

		result += "************************** Properties ***************\n";
		Properties props = System.getProperties();
		for (Object key : props.keySet()) {
			result += key + " = " + props.get(key) + "\n";
		}

		result += "************************** Map VCAP ***************\n";
		Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
		if (vcap != null) {
			for (String element : vcap.keySet()) {
				result += element + " = " + vcap.get(element) + "\n";
				// if there is a redis service bound, get the info
				if (element.equals("p-redis")) {
					System.out
							.println("************* I have a REDIS Config! ****");

					ArrayList<Object> a = (ArrayList<Object>) vcap.get(element);
					redisConfig = getRedisConfig((LinkedHashMap) a.get(0));
					System.out
							.println(getRedisConfig((LinkedHashMap) a.get(0)));
				}
			}
		}

		result += "************************** RedisConfig Parsed ***************\n";
		result += redisConfig;

		// System.out.println("Going to return: " + result);
		Credentials creds = redisConfig.getCredentials();

		String redisURIString = "redis://" + creds.getPassword() + "@"
				+ creds.getHost() + ":" + creds.getPort();

		result += "Redis URI is: " + redisURIString + "\n";

		ObjectMapper mapper = new ObjectMapper();
		JsonNode subNode = mapper.createObjectNode();

		((ObjectNode) subNode).put("rawdata", JsonEncode(result));

		try {
			log.debug(mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(subNode));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		response.setResultData(subNode);

		return response;
	}

	private Map<String, Object> mapVCAP(String vcapString) {

		Map<String, Object> vcap = null;

		if (vcapString != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				vcap = mapper.readValue(vcapString, Map.class);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			log.error("There was no VCAP environment variable.");
		}

		return vcap;

	}

	private String JsonEncode(String json) {
		json = json.replaceAll("\"", "\\\"");
		return json;
	}

	private RedisConfig getRedisConfig(HashMap jsonString) {

		System.out.println(" Going to try get the config for " + jsonString);
		RedisConfig rc = new RedisConfig();

		rc.setCredentials(new Credentials());
		rc.setLabel((String) jsonString.get("label"));
		rc.setName((String) jsonString.get("name"));
		rc.setPlan((String) jsonString.get("name"));
		HashMap<String, Object> creds = (HashMap<String, Object>) jsonString
				.get("credentials");
		System.out.println("Credentials = "
				+ jsonString.get("credentials").getClass());
		rc.getCredentials().setHost((String) creds.get("host"));
		rc.getCredentials().setPassword((String) creds.get("password"));
		rc.getCredentials().setPort((Integer) creds.get("port"));

		return rc;

	}

	private RedisClient getClient() {

		RedisConfig redisConfig = null;
		RedisClient redisClient = null;
		Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
		if (vcap != null) {
			for (String element : vcap.keySet()) {

				// if there is a redis service bound, get the info
				if (element.equals("p-redis")) {
					log.debug("I have a REDIS Config! ****");

					ArrayList<Object> a = (ArrayList<Object>) vcap.get(element);
					redisConfig = getRedisConfig((LinkedHashMap) a.get(0));

				}
			}

			Credentials creds = redisConfig.getCredentials();

			String redisURIString = "redis://" + creds.getPassword() + "@"
					+ creds.getHost() + ":" + creds.getPort();
			RedisURI redisUri = RedisURI.create(redisURIString);
			redisClient = RedisClient.create(redisUri);
		}
		return redisClient;

	}

	public GenericResponse ping() {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");
		RedisClient client = getClient();

		if (client != null) {
			StatefulRedisConnection<String, String> connection = client
					.connect();
			// Use the synchronous connection to do a ping.
			String pingResult = connection.sync().ping();
			connection.close();
			client.shutdown();

			ObjectMapper mapper = new ObjectMapper();
			// JsonNode rootNode = mapper.createObjectNode(); // will be of type
			// // ObjectNode
			// ((ObjectNode) rootNode).put("ResultData", "Tatu");

			JsonNode subNode = mapper.createObjectNode();
			((ObjectNode) subNode).put("Ping", pingResult);
			((ObjectNode) subNode).put("used_memory", "836048");
			// ((ObjectNode) rootNode).put("Memory", subNode);

			try {
				log.debug(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(subNode));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			response.setResultData(subNode);

			log.info(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
			log.error("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
		}

		return response;

	}

	private JsonNode info2json(String infoString) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		String[] lines = infoString.split("\n");

		JsonNode currentSubnode = mapper.createObjectNode();
		String currentSection = "";
		for (int x = 0; x < lines.length; x++) {
			String line = lines[x];

			// if it is a section header then make a parent node
			if (line.startsWith("#")) {
				System.out.println("Section start: " + line);
				currentSubnode = mapper.createObjectNode();
				currentSection = line.substring(1).trim();
				((ObjectNode) rootNode).put(currentSection, currentSubnode);
			} else {
				// if it isn't a tag:value line, don't add it
				int colon = line.indexOf(":");
				if (colon > -1) {
					String tag = line.substring(0, colon);
					String value = line.substring(colon + 1);
					tag = tag.trim();
					value = value.trim();

					((ObjectNode) currentSubnode).put(tag, value);
				}
			}

		}

		return rootNode;
	}

	public GenericResponse info() {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");
		RedisClient client = getClient();
		if (client != null) {
			StatefulRedisConnection<String, String> connection = client
					.connect();
			// Use the synchronous connection to do an info request.
			String infoResult = connection.sync().info();
			connection.close();
			client.shutdown();

			// convert the result to json
			JsonNode infoNode = info2json(infoResult);

			try {
				ObjectMapper mapper = new ObjectMapper();
				log.debug(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(infoNode));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			response.setResultData(infoNode);

			log.debug(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
			log.error("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
		}

		return response;

	}

	public GenericResponse info(String service) {

		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");
		RedisClient client = getClient();
		if (client != null) {
			StatefulRedisConnection<String, String> connection = client
					.connect();
			// Use the synchronous connection to do an info request.
			String infoResult = connection.sync().info(service);
			connection.close();
			client.shutdown();

			// convert the result to json
			JsonNode infoNode = info2json(infoResult);

			try {
				ObjectMapper mapper = new ObjectMapper();
				log.debug(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(infoNode));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			response.setResultData(infoNode);

			log.debug(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
			log.error("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
		}

		return response;

	}

}
