package info.unterrainer.websocketclient;

import org.junit.jupiter.api.Test;

public class WebSocketClientTest {

	@Test
	public void connectNormalWs() throws InterruptedException {
		try (WebsocketConnection client = WebsocketConnection.builder()
				.host("ws://localhost:7070/ws")
				.onMessageHandler(message -> System.out.println("Received message: " + message))
				.build()) {
			client.establish();

			client.send("Hello WebSocket!");
			client.send("another hello");

			Thread.sleep(500);
		}
	}

	@Test
	public void connectOauthWss() throws InterruptedException {
		try (WebsocketConnection client = WebsocketConnection.builder()
				.host("ws://localhost:7070/jwt")
				.onMessageHandler(message -> System.out.println("Received message: " + message))
				.keycloakHost("https://keycloak.lan.elite-zettl.at")
				.keycloakClient("Cms")
				.keycloakUser("test@cms-building.at")
				.keycloakPassword("test")
				.build()) {
			client.establish();

			client.send("Hello WebSocket!");
			client.send("another hello");

			Thread.sleep(500);
		}
	}

	@Test
	public void connectOauthWssExternal() throws InterruptedException {
		try (WebsocketConnection client = WebsocketConnection.builder()
				.host("wss://cms-llm.lan.elite-zettl.at/llm")
				.onMessageHandler(message -> System.out.println("Received message: " + message))
				.keycloakHost("https://keycloak.lan.elite-zettl.at")
				.keycloakClient("Cms")
				.keycloakUser("test@cms-building.at")
				.keycloakPassword("test")
				.build()) {
			client.establish();

			client.send("Hello WebSocket!");
			client.send("another hello");

			Thread.sleep(500);
		}
	}
}
