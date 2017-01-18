package my.util;

/**
 * is stand for a key, use for Map structure's key. useful for get a know Object
 * type from Map structure container.
 * Map<KeyConstants<?>, ?> map = new HashMap<>();
 * @author hubert
 */
public class KeyConstants<T> {
	/**
	 * constant name.
	 */
	private String name;
	
	public KeyConstants(String pName) {
		this.name = pName;
	}
	
	public String getName() {
		return name;
	}
}
