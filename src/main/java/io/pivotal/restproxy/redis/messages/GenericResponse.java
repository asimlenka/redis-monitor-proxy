package io.pivotal.restproxy.redis.messages;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericResponse {

	private String resultCode;

	private String resultMessage;

	private JsonNode resultData;

	public JsonNode getResultData() {
		return resultData;
	}

	public void setResultData(JsonNode resultData) {
		this.resultData = resultData;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	// public JsonNode getElementValueAsJSON() {
	// ObjectMapper mapper = new ObjectMapper();
	// // System.out.println("The element value is: " + getElementValue());
	// JsonNode node = null;
	// try {
	// node = mapper.readTree(getElementValue());
	// } catch (JsonProcessingException e) {
	// // Don't do anything -- if we can't parse it, it isn't JSON
	//
	// } catch (IOException e) {
	// // Don't do anything -- if we can't parse it, it isn't JSON
	// }
	//
	// return node;
	// }

	public String toJSON() {
		String result = "";
		ObjectMapper mapper = new ObjectMapper();

		try {

			String jsonStat = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(this);

			result = jsonStat;

		} catch (Exception e) {

			e.printStackTrace();

		}

		return result;

	}

}
