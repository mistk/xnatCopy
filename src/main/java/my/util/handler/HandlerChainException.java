package my.util.handler;

public class HandlerChainException extends Exception {

	public HandlerChainException(String errorMsg) {
		super(errorMsg);
	}

	public HandlerChainException(String errorMsg, Exception e) {
		super(errorMsg, e);
	}

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5676085905624818221L;
	
}
