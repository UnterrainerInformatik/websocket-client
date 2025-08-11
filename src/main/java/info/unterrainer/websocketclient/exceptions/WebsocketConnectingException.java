package info.unterrainer.websocketclient.exceptions;

public class WebsocketConnectingException extends WebsocketException {

	private static final long serialVersionUID = 2783771228373284782L;

	public WebsocketConnectingException() {
		super();
	}

	public WebsocketConnectingException(String message) {
		super(message);
	}

	public WebsocketConnectingException(Throwable cause) {
		super(cause);
	}

	public WebsocketConnectingException(String message, Throwable cause) {
		super(message, cause);
	}
}
