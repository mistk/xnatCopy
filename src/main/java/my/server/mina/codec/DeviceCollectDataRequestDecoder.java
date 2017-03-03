package my.server.mina.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import my.server.mina.request.DeviceCollectDataRequest;

/**
 * decode rule, convert origin byte to high-level object.
 * @author xnat
 *
 */
public class DeviceCollectDataRequestDecoder extends CumulativeProtocolDecoder {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		logger.trace("decode start...");
		// TODO write request data decode rule.
//		in.remaining()
		// TODO validate origin data.
		in.position(in.limit());
		DeviceCollectDataRequest request = new DeviceCollectDataRequest();
		request.setDeviceId(1111);
		out.write(request);
		logger.trace("decode end...");
		return true;
	}
	
}
