package io.pivotal.restproxy.redis.messages;

public class Credentials {

	@Override
	public String toString() {
		return "Credentials [host=" + host + ", password=" + password
				+ ", port=" + port + "]";
	}

	private String host;

	private String password;

	private int port;

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
