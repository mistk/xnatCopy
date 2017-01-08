package my.ioServer.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceCollectDataRequestDecoder extends CumulativeProtocolDecoder {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		logger.trace("decode start...");
		// TODO Auto-generated method stub
//		in.remaining()
		in.position(in.limit());
		logger.trace("decode end...");
		return true;
	}
	
}
