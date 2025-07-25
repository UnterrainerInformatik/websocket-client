package info.unterrainer.websocketclient;

public class SendingMessageWebsocketException extends RuntimeException {

	public SendingMessageWebsocketException() {
		super();
	}

	public SendingMessageWebsocketException(String message) {
		super(message);
	}

	public SendingMessageWebsocketException(Throwable cause) {
		super(cause);
	}

	public SendingMessageWebsocketException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 661400825771800217L;

}
