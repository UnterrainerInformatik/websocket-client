package info.unterrainer.websocketclient;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.glassfish.tyrus.client.ClientManager;

import info.unterrainer.commons.serialization.jsonmapper.JsonMapper;
import info.unterrainer.oauthtokenmanager.LocalOauthTokens;
import info.unterrainer.oauthtokenmanager.OauthTokenManager;
import info.unterrainer.websocketclient.exceptions.WebsocketClosingException;
import info.unterrainer.websocketclient.exceptions.WebsocketConnectingException;
import info.unterrainer.websocketclient.exceptions.WebsocketSendingMessageException;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
@EqualsAndHashCode()
public class WebsocketConnection implements AutoCloseable {

	@Getter
	private final String host;
	private final Consumer<EventContext> onOpenHandler;
	private final Consumer<EventContext> onMessageHandler;
	private final Consumer<EventContext> onBinaryMessageHandler;
	private final Consumer<EventContext> onCloseHandler;
	private final Consumer<EventContext> onErrorHandler;
	private final String keycloakHost;
	private final String keycloakClient;
	private final String keycloakClientSecret;
	private final String keycloakUser;
	private final String keycloakPassword;

	private WebsocketEndpoints endpoints;

	@lombok.Builder.Default
	private CompletableFuture<Session> sessionReady = new CompletableFuture<>();
	@lombok.Builder.Default
	private JsonMapper jsonMapper = JsonMapper.create();

	public Session awaitOpen(Duration timeoutInMillis) {
		try {
			if (timeoutInMillis == null) {
				// Blocks indefinitely until the session is ready.
				return sessionReady.get();
			}
			return sessionReady.get(timeoutInMillis.toMillis(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new IllegalStateException("WebSocket did not open in time.", e);
		}
	}

	/**
	 * Sends a ping message to the server to keep the connection alive.
	 */
	public void sendPing() {
		Session s = awaitOpen(Duration.ofMillis(5000L));
		try {
			s.getBasicRemote().sendPing(ByteBuffer.allocate(1));
		} catch (Exception e) {
			log.error("Error sending ping: ", e);
			throw new WebsocketSendingMessageException(String.format("Failed to send ping."), e);
		}
		log.debug("Sent ping");
	}

	public void send(String message) {
		Session s = awaitOpen(Duration.ofMillis(5000L));
		try {
			s.getBasicRemote().sendText(message);
		} catch (Exception e) {
			log.error("Error sending message: ", e);
			throw new WebsocketSendingMessageException(String.format("Failed to send message [%s].", message), e);
		}
		log.debug("Sent message: " + message);
	}

	public void send(byte[] message) {
		Session s = awaitOpen(Duration.ofMillis(5000L));
		try {
			s.getBasicRemote().sendBinary(java.nio.ByteBuffer.wrap(message));
		} catch (Exception e) {
			log.error("Error sending binary message: ", e);
			throw new WebsocketSendingMessageException("Failed to send binary message.", e);
		}
		log.debug("Sent binary message of length: " + message.length);
	}

	public <T> void send(T message) {
		Session s = awaitOpen(Duration.ofMillis(5000L));
		try {
			s.getBasicRemote().sendText(jsonMapper.toStringFrom(message));
		} catch (Exception e) {
			log.error("Error sending message: ", e);
			throw new WebsocketSendingMessageException(String.format("Failed to send message [%s].", message), e);
		}
		log.debug("Sent message: " + message);
	}

	@Override
	public void close() {
		Session s = awaitOpen(Duration.ofMillis(5000L));
		try {
			s.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure"));
		} catch (IOException e) {
			log.error("Error closing session.", e);
			throw new WebsocketClosingException("Failed to close WebSocket session.", e);
		}
	}

	public void establish() {
		String at = null;
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

		if (keycloakHost != null) {
			OauthTokenManager tokenManager = new OauthTokenManager(keycloakHost, keycloakClient);
			LocalOauthTokens tokens = tokenManager.getTokensFromCredentials(keycloakClient, keycloakUser,
					keycloakPassword);
			accessToken = tokens.getAccessToken();
			at = "Bearer " + accessToken;
		}

		try {
			container.connectToServer(endpoints,
					ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
					}).build(), URI.create(host));
			if (at != null) {
				Session s = awaitOpen(Duration.ofMillis(5000L));
				s.getBasicRemote().sendText(at);
			} else {
				log.debug("No access token provided, connecting without authentication.");
			}
		} catch (Exception e) {
			log.error("Error connecting to WebSocket server: ", e);
			throw new WebsocketConnectingException("Failed to connect to WebSocket server at " + host, e);
		}
		log.info("WebSocket client connected to: {}", host);
	}
}
