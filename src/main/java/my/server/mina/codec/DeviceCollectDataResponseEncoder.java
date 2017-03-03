package my.server.mina.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceCollectDataResponseEncoder extends ProtocolEncoderAdapter {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		logger.trace("encode start, message: {}", message);
		// TODO Auto-generated method stub
		logger.trace("encode end...");
	}

}
