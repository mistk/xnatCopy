package my.ioServer.handler;

import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import my.util.handler.AbstractProcessor;
import my.util.handler.HandlerChain;
import my.util.handler.HandlerResult;
import my.util.handler.Processor;

@Component(ResponseProcessor.COMPONENT_NAME)
public class ResponseProcessor extends AbstractProcessor {
    public static final String COMPONENT_NAME = Processor.NAMESPACE + ".ResponseProcessor";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public int process(Map<Object, Object> params, HandlerResult handlerResult) {
		logger.trace("Response Processor start...");
		logger.debug("current chain name: {}", params.get(HandlerChain.PARAM_CHAIN_NAME));
		int result = HandlerChain.CODE_CONTINUE;
		IoSession session = (IoSession) params.get(IoHandlerConstants.PARAM_IOSESSION);
		// TODO session write response.
		logger.trace("Response Processor end...");
		return result;
	}

}
