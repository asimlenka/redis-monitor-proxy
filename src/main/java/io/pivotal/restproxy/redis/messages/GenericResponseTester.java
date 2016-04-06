package io.pivotal.restproxy.redis.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GenericResponseTester {

	public static void main(String[] args) throws JsonProcessingException {
		GenericResponse response = new GenericResponse();

		response.setResultCode("200");
		response.setResultMessage("It all finished fine");

		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.createObjectNode(); // will be of type
														// ObjectNode
		((ObjectNode) rootNode).put("name", "Tatu");

		JsonNode subNode = mapper.createObjectNode();
		((ObjectNode) subNode).put("used_memory", "836048");
		((ObjectNode) rootNode).put("Memory", subNode);

		System.out.println(mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(rootNode));

		response.setResultData(rootNode);

		System.out.println(response.toJSON());

		System.out.println("********** JSON Node ************");

		String input = "# Replication\n\r";
		input += "role:master\n";
		input += "connected_slaves:0\r\n\n";
		input += "# Donkey\n";
		input += "llama:thunderchicken\r\n\n";

		JsonNode j = info2json(input);

		System.out.println(mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(j));

	}

	public static JsonNode info2json(String infoString) {
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

}
