package my.util;

import java.util.HashMap;
import java.util.Map;

/**
 * is stand for a key, use for Map structure's key. useful for get a know Object
 * type from Map structure container.
 * Map<KeyConstants<?>, ?> map = new HashMap<>();
 * @author hubert
 */
public class KeyConstants<T> {
    /**
     * constant reference.
     */
    private static final Map<String, KeyConstants<?>> STRING2CONSTANT = new HashMap<>();
	/**
	 * constant name.
	 */
	private String name;
	
	public KeyConstants(String pName) {
		this.name = pName;
		STRING2CONSTANT.put(pName, this);
	}
	
	public String getName() {
		return name;
	}
	
	public static KeyConstants<?> getConstant(String pName) {
	    return STRING2CONSTANT.get(pName);
	}
}
