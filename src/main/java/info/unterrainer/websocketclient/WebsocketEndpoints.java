package info.unterrainer.websocketclient;

import java.time.Duration;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebsocketEndpoints extends Endpoint {

	private final WebsocketConnection client;

	public WebsocketEndpoints(WebsocketConnection client) {
		this.client = client;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		client.sessionReady.complete(session);
		log.info("Connected to server");

		// onOpen-Handler
		if (client.onOpenHandler != null) {
			try {
				client.onOpenHandler.accept(session);
			} catch (Exception e) {
				log.error("Error executing onOpen handler: ", e);
			}
		}

		// onMessage-Handler
		session.addMessageHandler(String.class, message -> {
			log.debug("Received message: " + message);
			client.awaitOpen(Duration.ofMillis(5000L));
			if (client.onMessageHandler != null) {
				try {
					client.onMessageHandler.accept(message);
				} catch (Exception e) {
					log.error("Error executing onMessage handler: ", e);
				}
			}
		});
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		Session s = client.awaitOpen(Duration.ofMillis(5000L));
		log.info("Disconnected from server: {}", closeReason);
		if (client.onCloseHandler != null) {
			try {
				client.onCloseHandler.accept(s);
			} catch (Exception e) {
				log.error("Error executing onClose handler: ", e);
			}
		}

		try {
			s.close();
		} catch (Exception e) {
			log.error("Error closing session: ", e);
		}
	}

	@Override
	public void onError(Session session, Throwable throwable) {
		client.awaitOpen(Duration.ofMillis(5000L));
		log.error("Error occurred: ", throwable);
		if (client.onErrorHandler != null) {
			try {
				client.onErrorHandler.accept(throwable);
			} catch (Exception e) {
				log.error("Error executing onError handler: ", e);
			}
		}
	}
}
