package info.unterrainer.websocketclient;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.glassfish.tyrus.client.ClientManager;

import info.unterrainer.oauthtokenmanager.LocalOauthTokens;
import info.unterrainer.oauthtokenmanager.OauthTokenManager;
import info.unterrainer.websocketclient.exceptions.WebsocketClosingException;
import info.unterrainer.websocketclient.exceptions.WebsocketConnectingException;
import info.unterrainer.websocketclient.exceptions.WebsocketSendingMessageException;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ClientEndpointConfig.Builder;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode()
public class WebsocketConnection implements AutoCloseable {

	private final String host;
	final Consumer<Session> onOpenHandler;
	final Consumer<String> onMessageHandler;
	final Consumer<Session> onCloseHandler;
	final Consumer<Throwable> onErrorHandler;
	final String keycloakHost;
	final String keycloakClient;
	final String keycloakClientSecret;
	final String keycloakUser;
	final String keycloakPassword;

	private WebsocketEndpoints endpoints;

	final CompletableFuture<Session> sessionReady = new CompletableFuture<>();

	public Session awaitOpen() {
		try {
			return sessionReady.get();
		} catch (Exception e) {
			throw new IllegalStateException("WebSocket did not open in time.", e);
		}
	}

	public void send(String message) {
		Session s = awaitOpen();
		try {
			s.getBasicRemote().sendText(message);
		} catch (Exception e) {
			log.error("Error sending message: ", e);
			throw new WebsocketSendingMessageException(String.format("Failed to send message [%s].", message), e);
		}
		log.debug("Sent message: " + message);
	}

	@Override
	public void close() {
		Session s = awaitOpen();
		try {
			s.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure"));
		} catch (IOException e) {
			log.error("Error closing session.", e);
			throw new WebsocketClosingException("Failed to close WebSocket session.", e);
		}
	}

	public void establish() {
		String accessToken = null;
		log.debug("Establish called.");

		ClientManager container;
		try {
			container = ClientManager.createClient();
			log.info("ClientManager created");
		} catch (Exception e) {
			log.error("Failed to create ClientManager: {}", e.getMessage(), e);
			throw new WebsocketConnectingException("Failed to create WebSocket ClientManager.", e);
		}
		endpoints = new WebsocketEndpoints(this);
		Builder c = ClientEndpointConfig.Builder.create();

		if (keycloakHost != null) {
			OauthTokenManager tokenManager = new OauthTokenManager(keycloakHost, keycloakClient);
			LocalOauthTokens tokens = tokenManager.getTokensFromCredentials(keycloakClient, keycloakUser,
					keycloakPassword);
			accessToken = tokens.getAccessToken();
			String at = "Bearer " + accessToken;

			c.configurator(new ClientEndpointConfig.Configurator() {
				@Override
				public void beforeRequest(Map<String, List<String>> headers) {
					headers.put("Authorization", List.of(at));
				}
			});
		}
		ClientEndpointConfig config = c.build();

		try {
			container.connectToServer(endpoints, config, URI.create(host));
		} catch (Exception e) {
			log.error("Error connecting to WebSocket server: ", e);
			throw new WebsocketConnectingException("Failed to connect to WebSocket server at " + host, e);
		}
		log.info("WebSocket client connected to: {}", host);
	}
}
