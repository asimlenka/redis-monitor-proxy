package io.pivotal.restproxy.redis;

import io.pivotal.restproxy.redis.messages.Credentials;
import io.pivotal.restproxy.redis.messages.GenericResponse;
import io.pivotal.restproxy.redis.messages.RedisConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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

	private ApplicationContext ctx;

	public ApplicationContext getCtx() {
		return ctx;
	}

	public void setCtx(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	public String checkHealth() {

		String result = "";
		GenericResponse response = new GenericResponse();
		response.setResultCode("200");
		response.setResultMessage("Connection Successful");

		RedisConfig redisConfig = null;

		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			result += beanName + "\n";
			System.out.println(beanName);
		}

		result += "************************** Environment Vars ***************\n";
		Map<String, String> envs = System.getenv();
		for (String key : envs.keySet()) {
			result += key + " = " + envs.get(key) + "\n";
			// System.out.println(key + " = " + envs.get(key));
		}

		result += "************************** Properties ***************\n";
		Properties props = System.getProperties();
		for (Object key : props.keySet()) {
			result += key + " = " + props.get(key) + "\n";
			// System.out.println(key +" = " + props.getProperty((String)key));
		}

		System.out.println("Going to Map the VCAP");
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

		RedisURI redisUri = RedisURI.create(redisURIString);

		// Cluster connect

		RedisClient clusterClient = RedisClient.create(redisUri);
		// ClientResources resources = clusterClient.getResources();

		StatefulRedisConnection<String, String> connection = clusterClient
				.connect();
		for (Object o : connection.sync().command()) {
			System.out.println(o);
		}

		System.out.println("Got the cluster! " + connection.isOpen());
		System.out.println("Clients : " + connection.sync().clientList());
		System.out.println("Ping Result: " + connection.sync().ping());
		System.out.println("INFO: " + connection.sync().info());
		System.out.println("Size: " + connection.sync().dbsize());
		result += " Client Options &&&&&&&&&&&&&&&& " + connection;

		ObjectMapper mapper = new ObjectMapper();

		JsonNode subNode = mapper.createObjectNode();

		((ObjectNode) subNode).put("rawdata", result);

		try {
			System.out.println(mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(subNode));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		response.setResultData(subNode);

		return result;
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
			System.out.println("There was no VCAP env -----");
		}

		return vcap;

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

	public RedisClient getClient() {

		RedisConfig redisConfig = null;
		RedisClient redisClient = null;
		Map<String, Object> vcap = mapVCAP(System.getenv().get("VCAP_SERVICES"));
		if (vcap != null) {
			for (String element : vcap.keySet()) {

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
				System.out.println(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(subNode));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			response.setResultData(subNode);

			System.out.println(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
		}

		return response;

	}

	public JsonNode info2json(String infoString) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode();

		String[] lines = infoString.split("\n");
		System.out.println("There are " + lines.length);
		JsonNode currentSubnode = mapper.createObjectNode();
		String currentSection = "";
		for (int x = 0; x < lines.length; x++) {
			String line = lines[x];
			System.out.println("..." + line + "...");
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
					System.out.println("tag:" + tag + "!");
					System.out.println("value:" + value + "!");
					((ObjectNode) currentSubnode).put(tag, value);
				}
			}

		}

		System.out.println("There are " + lines.length);

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
				System.out.println(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(infoNode));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			response.setResultData(infoNode);

			System.out.println(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
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
				System.out.println(mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(infoNode));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			response.setResultData(infoNode);

			System.out.println(response.toJSON());
		} else {
			// There is no Redis service connected
			response.setResultCode("500");
			response.setResultMessage("Error: Could not bind to a Redis Service. Make sure a Redis service is bound to this application.");
		}

		return response;

	}

}
