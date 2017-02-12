package my.util.handler;

/**
 * define a class: implement Map<int, String>.
 * @author hubert
 *
 */
public class StatusCode {
	/**
	 * usually is method return a primitive int value.
	 */
	int code;
	/**
	 * a key stand for what. different environment should comment key is stand for what mean.
	 */
	String key;
	public StatusCode(int code) {
		this.code = code;
	}
	public StatusCode(int code, String key) {
		this.code = code;
		this.key = key;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		// code identify this object.
		if (obj instanceof StatusCode) {
			return ((StatusCode) obj).code == this.code;
		}
		return false;
	}
}
