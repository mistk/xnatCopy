package my.util.handler;

import java.util.Map;

/**
 * scope is global.
 * Logical split.
 * please extends AbstractProcessor.
 * @author hubert
 *
 */
public interface Processor {
    public static final String NAMESPACE = "processor";
	/**
	 * processor name.
	 * note must not be blank. for identify.
	 * @return processor name.
	 */
	String getName();
	/**
	 * logic process method.
	 * @param params
	 * @param handlerResult HandlerResult
	 * @return
	 */
	int process(Map<Object, Object> params, HandlerResult handlerResult);
}
