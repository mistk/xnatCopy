package my.ioServer.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import my.util.handler.HandlerChain;
import my.util.handler.HandlerResult;
import my.util.handler.Processor;
import my.util.handler.StatusCode;

/**
 * Io handler, contains Executable Processor.
 * @author hubert
 *
 */
public class DeviceCollectDataIoHandler extends IoHandlerAdapter implements InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private HandlerChain messageReceivedHandlerChain;
	@Resource(name = ValidateProcessor.COMPONENT_NAME)
	private Processor validateProcessor;
	@Resource(name = RequestDataProcessor.COMPONENT_NAME)
	private Processor requestDataProcessor;
	@Resource(name = ResponseProcessor.COMPONENT_NAME)
	private Processor responseProcessor;
	
	public void afterPropertiesSet() throws Exception {
		logger.trace("Initializ bean IoHandler start...");
		messageReceivedHandlerChain = new HandlerChain(3)
				.setName(new StrBuilder().append(HandlerChain.CHAIN_NAMESPACE).append(".").append(getClass().getSimpleName()).append(".messageReceivedHandlerChain").toString())
				.add(validateProcessor, new StatusCode(1, RequestDataProcessor.class.getSimpleName()))
				.add(requestDataProcessor)
				.add(responseProcessor);
		logger.info("init a HandlerChain: {}", messageReceivedHandlerChain.getName());
		logger.trace("Initializ bean IoHandler end...");
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// TODO performance monitor.
		logger.trace("handle messageReceived event start, message: {}", message);
		logger.debug("handler messageReceived event, IoSessionId: {}", session.getId());
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(IoHandlerConstants.PARAM_IOSESSION, session);
		params.put(IoHandlerConstants.PARAM_MESSAGE, message);
		HandlerResult handlerResult = messageReceivedHandlerChain.run(params);
		// TODO handle result.
		logger.trace("handle messageReceived event start end");
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("exception happen: ", cause);
	}
	
	public HandlerChain getMessageReceivedHandlerChain() {
		return messageReceivedHandlerChain;
	}
}
