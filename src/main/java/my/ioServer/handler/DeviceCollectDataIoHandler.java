package my.ioServer.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.ChainedIoHandler;
import org.apache.mina.handler.chain.IoHandlerChain.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Io handler, contains Executable Commands.
 * @author xnat
 *
 */
public class DeviceCollectDataIoHandler extends ChainedIoHandler implements InitializingBean {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// TODO performance monitor.
		logger.trace("handler chain process start, message: {}", message);
		super.messageReceived(session, message);
		logger.trace("handler chain process end");
	}

	public void afterPropertiesSet() throws Exception {
		logger.trace("Initializ bean IoHandler start...");
		getChain().addFirst(ValidateCommand.class.getSimpleName(), new ValidateCommand());
		getChain().addLast(PersistCommand.class.getSimpleName(), new PersistCommand());
		// TODO logger debug.
//		if (logger.isDebugEnabled()) {
//			List<Entry> commands = getChain().getAll();
//			List<String> commandsStr = new ArrayList<String>(commands.size());
//			if (CollectionUtils.isNotEmpty(commands)) {
//				for (Entry e : commands) {
//					commandsStr.add(e.getName());
//				}
//			}
//			logger.debug("Handler chain Execute Commands: {}", commandsStr);
//		}
		logger.trace("Initializ bean IoHandler end...");
	}
}
