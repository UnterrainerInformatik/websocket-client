package info.unterrainer.websocketclient;

import info.unterrainer.commons.serialization.jsonmapper.JsonMapper;
import jakarta.websocket.Session;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@RequiredArgsConstructor
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
public class EventContext {

	private final Session session;
	private final JsonMapper jsonMapper;

	private String message;
	@lombok.Builder.Default
	private byte[] binaryMessage = new byte[0];
	private Throwable error;

	public <T> T message(Class<T> type) {
		return jsonMapper.fromStringTo(type, message);
	}
}
