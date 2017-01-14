package my.ioServer.handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import my.util.handler.AbstractProcessor;
import my.util.handler.HandlerChain;
import my.util.handler.HandlerResult;

/**
 * validate action.
 * @author xnat
 *
 */
@Component
public class ValidateProcessor extends AbstractProcessor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public int process(Map<Object, Object> params, HandlerResult handlerResult) {
		logger.trace("Validate Processor start...");
		logger.debug("current chain name: {}", params.get(HandlerChain.PARAM_CHAIN_NAME));
		int result = HandlerChain.CODE_CONTINUE;
//		Object message = params.get(IoHandlerConstants.PARAM_MESSAGE);
		logger.trace("Validate Processor end...");
		result = 2;
		return result;
	}

}
