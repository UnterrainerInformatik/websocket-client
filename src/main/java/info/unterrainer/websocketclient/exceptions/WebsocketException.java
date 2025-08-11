package info.unterrainer.websocketclient.exceptions;

public class WebsocketException extends RuntimeException {

	private static final long serialVersionUID = -700574755132411939L;

	public WebsocketException() {
		super();
	}

	public WebsocketException(String message) {
		super(message);
	}

	public WebsocketException(Throwable cause) {
		super(cause);
	}

	public WebsocketException(String message, Throwable cause) {
		super(message, cause);
	}

}
