package my.ioServer.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * validate action.
 * @author xnat
 *
 */
public class ValidateCommand implements IoHandlerCommand {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void execute(NextCommand next, IoSession session, Object message) throws Exception {
		logger.trace("start validate command...");
		// TODO.
		// if some condition not support.
//		session.write("");
		next.execute(session, message);
		logger.trace("end validate command...");
	}

}
