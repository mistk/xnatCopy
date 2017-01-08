package my.ioServer;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.ChainedIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DeviceCollectDataIoHandler extends ChainedIoHandler implements InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.trace("handler chain process start, message: {}", message);
		super.messageReceived(session, message);
		logger.trace("handler chain process end");
	}

	public void afterPropertiesSet() throws Exception {
		logger.trace("Initializ bean IoHandler start...");
//		getChain().addFirst(name, command);
		logger.trace("Initializ bean IoHandler end...");
	}
}
