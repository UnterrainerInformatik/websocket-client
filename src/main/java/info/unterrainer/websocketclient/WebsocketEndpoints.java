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
	private final String name;

	public WebsocketEndpoints(WebsocketConnection client, String name) {
		this.client = client;
		this.name = name;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		client.sessionReady().complete(session);
		session.setMaxIdleTimeout(0L);
		log.info("(" + name + ") Connected to server");
		if (client.onOpenHandler() != null) {
			try {
				client.onOpenHandler()
						.accept(EventContext.builder().session(session).jsonMapper(client.jsonMapper()).build());
			} catch (Exception e) {
				log.error("(" + name + ") Error executing onOpen handler: ", e);
			}
		}

		// onMessage-Handler
		session.addMessageHandler(String.class, message -> {
			log.debug("(" + name + ") Received message: " + message);
			client.awaitOpen(Duration.ofMillis(5000L));
			if (client.onMessageHandler() != null) {
				try {
					client.onMessageHandler()
							.accept(EventContext.builder()
									.session(session)
									.jsonMapper(client.jsonMapper())
									.message(message)
									.build());
				} catch (Exception e) {
					log.error("(" + name + ") Error executing onMessage handler: ", e);
				}
			}
		});

		session.addMessageHandler(byte[].class, message -> {
			log.debug("(" + name + ") Received binary-message: " + message);
			client.awaitOpen(Duration.ofMillis(5000L));
			if (client.onBinaryMessageHandler() != null) {
				try {
					client.onBinaryMessageHandler()
							.accept(EventContext.builder()
									.session(session)
									.jsonMapper(client.jsonMapper())
									.binaryMessage(message)
									.build());
				} catch (Exception e) {
					log.error("(" + name + ") Error executing onMessage handler: ", e);
				}
			}
		});
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		Session s = client.awaitOpen(Duration.ofMillis(5000L));
		log.info("(" + name + ") Disconnected from server: {}", closeReason);
		if (client.onCloseHandler() != null) {
			try {
				client.onCloseHandler()
						.accept(EventContext.builder().session(session).jsonMapper(client.jsonMapper()).build());
			} catch (Exception e) {
				log.error("(" + name + ") Error executing onClose handler: ", e);
			}
		}

		try {
			s.close();
		} catch (Exception e) {
			log.error("(" + name + ") Error closing session: ", e);
		}
	}

	@Override
	public void onError(Session session, Throwable throwable) {
		client.awaitOpen(Duration.ofMillis(5000L));
		log.error("(" + name + ") Error occurred: ", throwable);
		if (client.onErrorHandler() != null) {
			try {
				client.onErrorHandler()
						.accept(EventContext.builder()
								.session(session)
								.jsonMapper(client.jsonMapper())
								.error(throwable)
								.build());
			} catch (Exception e) {
				log.error("(" + name + ") Error executing onError handler: ", e);
			}
		}
	}
}
