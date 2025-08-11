package info.unterrainer.websocketclient.exceptions;

public class WebsocketSendingMessageException extends WebsocketException {

	private static final long serialVersionUID = 6354296246039531090L;

	public WebsocketSendingMessageException() {
		super();
	}

	public WebsocketSendingMessageException(String message) {
		super(message);
	}

	public WebsocketSendingMessageException(Throwable cause) {
		super(cause);
	}

	public WebsocketSendingMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
