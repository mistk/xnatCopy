package my.util.handler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 按一定的规则执行逻辑链 link:processors
 * 按Processor 返回的状态值不同变换执行规则.
 * 默认规则:
 * 	如果Processor 返回 0 顺序依次向下执行
 * 	如果Processor 返回 1 执行完毕,跳出执行链
 * 自定义规则: (see: statuses)
 * 	根据Processor 对应的 多个 StatusCode 找出匹配的状态码和下一个要执行的Processor执行.
 * @author hubert
 *
 */
public class HandlerChain {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * current handler chain name.
	 */
	private String name;
	/**
	 * Processor:list of StatusCode(code is processor return code, key is next Processor name).
	 */
	private final Map<Processor, List<StatusCode>> processors;
	/**
	 * chain init size.
	 */
	private final static int INIT_SIZE = 7;
	public final static int CODE_CONTINUE = 0;
	public final static int CODE_BREAK = -1;
	public static final String PARAM_CHAIN_NAME = "chainName";
	
	public HandlerChain() {
		this(INIT_SIZE);
	}
	public HandlerChain(int initSize) {
		processors = new LinkedHashMap<Processor, List<StatusCode>>(initSize);
	}
	public HandlerChain(String name) {
		this(INIT_SIZE);
		this.name = name;
	}
	/**
	 * constructor.
	 * @param name chain name.
	 * @param initSize init size
	 */
	public HandlerChain(String name, int initSize) {
		this(initSize);
		this.name = name;
	}
	
	/**
	 * add processor to this handler chain.
	 * if processor already exist in chain, will update it's statuses.
	 * @param processor Processor
	 * @param statuses
	 * @return
	 */
	public HandlerChain add(Processor processor, StatusCode ...statuses) {
		if (processor == null) {
			logger.warn("add processor is null, so ignore");
			return this;
		}
		Iterator<Entry<Processor, List<StatusCode>>> it = processors.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Processor, List<StatusCode>> entry = it.next();
			if (entry.getKey().equals(processor)) {
				logger.debug("processor:{} already exist in chain, will update it's statuses.", processor);
				it.remove();
				break;
			}
		}
		List<StatusCode> existStatuses = this.processors.get(processor);
		processors.put(processor, existStatuses);
		if (ArrayUtils.isNotEmpty(statuses)) {
			if (existStatuses == null) {
				existStatuses = new ArrayList<StatusCode>(5);
				processors.put(processor, existStatuses);
			}
			for (StatusCode newStatusCode : statuses) {
				Iterator<StatusCode> it2 = existStatuses.iterator();
				while (it2.hasNext()) {
					if (it2.next().equals(newStatusCode)) {
						logger.debug("processor: {} exist status code: {}, will overrride", processor, newStatusCode.code);
						it2.remove();
						break;
					}
				}
				// validate status key.
				if (StringUtils.isBlank(newStatusCode.key)) {
					logger.warn("new StatusCode.key is blank, so ignore");
					continue;
				}
				// if different code but same name is allow, but necessary ?
				existStatuses.add(newStatusCode);
			}
		}
		return this;
	}
	
	public HandlerResult run(Map<Object, Object> params) throws HandlerChainException {
		HandlerResult handlerResult = new HandlerResult();
		if (params == null) {
			handlerResult.setSuccess(false);
			return handlerResult;
		}
		if (StringUtils.isBlank(getName())) {
			logger.warn("current handler chain not exist name.");
		}
		logger.trace("handler chain:{} run start.", name);
		params.put(PARAM_CHAIN_NAME, name);
		Iterator<Entry<Processor, List<StatusCode>>> it = processors.entrySet().iterator();

		int preProcessStatus = 0;
		Entry<Processor, List<StatusCode>> preExceuteEntry = null;
		// first processor execute.
		if (it.hasNext()) {
			Entry<Processor, List<StatusCode>> entry = it.next();
			preProcessStatus = executeProcessor(entry.getKey(), params, handlerResult);
			preExceuteEntry = entry;
		} else {
			logger.error("current handler chain:{} not exist any processor. exit run", name);
			handlerResult.setSuccess(false);
			return handlerResult;
		}
		
		// default if pre processor return code is 0, execute left processor by natural order.
		// default if pre processor return code is 1, break execute chain.
		// otherwise until processor name equal left processor's name that will be execute.
		while (it.hasNext()) {
			Entry<Processor, List<StatusCode>> entry = it.next();
			if (preProcessStatus == CODE_CONTINUE) {
				preProcessStatus = executeProcessor(entry.getKey(), params, handlerResult);
			} else if (preProcessStatus == CODE_BREAK) {
				logger.debug("processor: {} break handler chain: {}", entry.getKey(), name);
				break;
			} else {
				List<StatusCode> statusCodes = preExceuteEntry.getValue();
				if (CollectionUtils.isEmpty(statusCodes)) {
					String errorMsg = new StrBuilder().append("processor: ").append(preExceuteEntry.getKey())
							.append(" returned code: ").append(preProcessStatus).append(" is not recognized").toString();
					throw new HandlerChainException(errorMsg);
				}
				Entry<Processor, List<StatusCode>> jumpEntry = null;
				for (StatusCode status : statusCodes) {
					if (preProcessStatus == status.code) {
						// There is no jump, if next processor is next one.
						if (StringUtils.equals(status.key, entry.getKey().getName())) {
							logger.debug("there is no jump, next processor is next one: {}", entry.getKey());
							preProcessStatus = executeProcessor(entry.getKey(), params, handlerResult);
							jumpEntry = entry;
							break;
						}
						while (it.hasNext()) {
							jumpEntry = it.next();
							if (StringUtils.equals(status.key, jumpEntry.getKey().getName())) {
								logger.debug("jump to processor: {}", jumpEntry.getKey());
								preProcessStatus = executeProcessor(jumpEntry.getKey(), params, handlerResult);
								break;
							}
						}
						break;
					}
				}
				if (jumpEntry == null) {
					handlerResult.setSuccess(false);
					throw new HandlerChainException(MessageFormat.format("processor: {0} return code: {1} is not exist mapped jump processor", preExceuteEntry.getKey(), preProcessStatus));
				} else {
					entry = jumpEntry;
				}
			}
			preExceuteEntry = entry;
		}
		logger.trace("handler chain: {} execute end.", name);
//		handlerResult.setSuccess(false);
		return handlerResult;
	}

	/**
	 * execute processor, wrap processor's exception.
	 * 
	 * @param processor
	 *            Processor
	 * @param params
	 *            Map<Object, Object>
	 * @param handlerResult
	 *            HandlerResult
	 * @return
	 * @throws HandlerChainException
	 */
	private int executeProcessor(Processor processor, Map<Object, Object> params, HandlerResult handlerResult) throws HandlerChainException {
		if (processor == null) {
			logger.error("parameter processor is null");
			return CODE_BREAK;
		}
		try {
			int processCode = processor.process(params, handlerResult);
			logger.debug("processor: {}, return code: {}", processor, processCode);
			return processCode;
		} catch (Exception e) {
			StrBuilder errorMsg = new StrBuilder().append("execute processor: {} error!", processor);
			throw new HandlerChainException(errorMsg.toString(), e);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}

	public HandlerChain setName(String name) {
		this.name = name;
		return this;
	}
}
