package my.ioServer.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import my.dao.mysql.entity.DeviceCollectDataEntity;
import my.ioServer.request.DeviceCollectDataRequest;
/**
 * persist message data to database.
 * @author xnat
 *
 */
import my.service.DeviceDataCollectService;
import my.util.handler.AbstractProcessor;
import my.util.handler.HandlerChain;
import my.util.handler.HandlerResult;
import my.util.handler.Processor;

@Component(RequestDataProcessor.COMPONENT_NAME)
public class RequestDataProcessor extends AbstractProcessor {
    public static final String COMPONENT_NAME = Processor.NAMESPACE+".RequestDataProcessor";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DeviceDataCollectService deviceDataCollectService;
	
	public int process(Map<Object, Object> params, HandlerResult handlerResult) {
		logger.trace("RequestData Processor start...");
		logger.debug("current chain name: {}", params.get(HandlerChain.PARAM_CHAIN_NAME));
		int result = HandlerChain.CODE_CONTINUE;
		Object message = params.get(IoHandlerConstants.PARAM_MESSAGE);
		boolean persistResult = deviceDataCollectService.collect(convertRequestDataToEntity(message));
		if (!persistResult) {
			result = 1;
		}
		logger.trace("RequestData Processor end...");
		return result;
	}
	

	/**
	 * convert DeviceCollectDataRequest to DeviceCollectDataEntity.
	 * @param message origin message.
	 * @return
	 */
	@SuppressWarnings({"rawtypes" })
	private List<DeviceCollectDataEntity> convertRequestDataToEntity(Object message) {
		if (message == null) {
			logger.error("paramter message object is null");
			return null;
		}
		List<DeviceCollectDataEntity> entities = null;
		if (message instanceof List) {
			List messages = (List) message;
			entities = new ArrayList<DeviceCollectDataEntity>(messages.size());
			for (Object obj : messages) {
				if (obj instanceof DeviceCollectDataRequest) {
					entities.add(copyToEntity((DeviceCollectDataRequest) obj));
				} else {
					// TODO
					logger.warn("unknow type exist, {}", obj);
				}
			}
		} else if (message instanceof DeviceCollectDataRequest) {
			entities = Collections.singletonList(copyToEntity((DeviceCollectDataRequest) message));
		} else {
			// TODO
			logger.warn("unknow decode type exist, {}", message);
		}
		return entities;
	}
	
	/**
	 * copy request Object data to persist Object.
	 * @param request DeviceCollectDataRequest
	 * @return DeviceCollectDataEntity
	 */
	private DeviceCollectDataEntity copyToEntity(DeviceCollectDataRequest request) {
		if (request == null) {
			logger.warn("paramter request object is null, ignore this request data");
			return null;
		}
		DeviceCollectDataEntity entity = new DeviceCollectDataEntity();
		// TODO copy properties.
		return entity;
	}

}
