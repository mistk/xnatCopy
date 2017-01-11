package my.ioServer.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.chain.IoHandlerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import my.dao.mysql.entity.DeviceCollectDataEntity;
import my.ioServer.request.DeviceCollectDataRequest;
/**
 * persist message data to database.
 * @author xnat
 *
 */
public class PersistCommand implements IoHandlerCommand {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void execute(NextCommand next, IoSession session, Object message) throws Exception {
		logger.trace("persist Command start...");
		// TODO Auto-generated method stub
		next.execute(session, message);
		logger.trace("persist Command end...");
	}

	/**
	 * copy request Object data to persist Object.
	 * @param request
	 * @return
	 */
	private DeviceCollectDataEntity copyToEntity(DeviceCollectDataRequest request) {
		DeviceCollectDataEntity entity = new DeviceCollectDataEntity();
		return entity;
	}
}
