package my.util.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Generator Context.
 * @author hubert
 */
public class GeneratorContext {
	/**
     * Object container.
     */
    private Map<Object, Object> context = new HashMap<>();

	/**
	 * put known type T data.
	 * 
	 * @param pKey
	 *            DTOConstants
	 * @param pValue
	 *            Object type is T
	 * @return Object type is T
	 */
	@SuppressWarnings("unchecked")
	public <T> T put(DTOConstants<T> pKey, T pValue) {
		return (T) getContext().put(pKey, pValue);
	}

	/**
	 * get T form context.
	 * 
	 * @param pKey
	 *            DTOConstants
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(DTOConstants<T> pKey) {
		return (T) getContext().get(pKey);
	}
	
	@Override
    public String toString() {
        return "GeneratorContext [context=" + context + "]";
    }
	
    public Map<Object, Object> getContext() {
		return context;
	}
}
