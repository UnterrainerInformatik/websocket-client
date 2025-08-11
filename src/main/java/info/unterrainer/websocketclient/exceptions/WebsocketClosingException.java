package info.unterrainer.websocketclient.exceptions;

public class WebsocketClosingException extends WebsocketException {

	private static final long serialVersionUID = -8862710911083647793L;

	public WebsocketClosingException() {
		super();
	}

	public WebsocketClosingException(String message) {
		super(message);
	}

	public WebsocketClosingException(Throwable cause) {
		super(cause);
	}

	public WebsocketClosingException(String message, Throwable cause) {
		super(message, cause);
	}
}
