package my.autoconfigure;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import my.constanst.CommonConstants;
import my.ioServer.codec.DeviceCollectDataCodecFactory;
import my.ioServer.handler.DeviceCollectDataIoHandler;

@Configuration
@EnableConfigurationProperties(IoServerProperties.class)
@ConditionalOnClass(IoAcceptor.class)
public class IoAcceptorAutoConfigure {
	private static final String FILTER_NAME_LOGGER = "logger";
	private static final String FILTER_NAME_CODEC = "codec";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "ioServer.filterLogConfig")
	public LoggingFilter loggingFilter(IoServerProperties properties) {
		logger.trace("init bean LoggingFilter start...");
		LoggingFilter loggingFilter = new LoggingFilter();
		logger.trace("init bean LoggingFilter end...");
		return loggingFilter;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public IoHandler ioHandler() {
		logger.trace("init bean IoHandler start...");
		// TODO custom handler.
		IoHandler ioHandler = new DeviceCollectDataIoHandler();
		//IoHandlerAdapter/ChainedIoHandler
		logger.trace("init bean IoHandler end...");
		return ioHandler;
	}

	@Bean
	@ConditionalOnMissingBean
	public IoAcceptor ioAcceptor(IoServerProperties properties) {
		logger.trace("init bean IoAcceptor start...");
		IoAcceptor acceptor = new NioSocketAcceptor();
		// SimpleIoProcessorPool include some session processor, every processor process session.
		// org.apache.mina.core.polling.AbstractPollingIoProcessor.process(S) invoke filters.
		acceptor.getFilterChain().addLast(FILTER_NAME_LOGGER, loggingFilter(properties));
//		acceptor.getFilterChain().addLast(FILTER_NAME_CODEC, new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		acceptor.getFilterChain().addLast(FILTER_NAME_CODEC, new ProtocolCodecFilter(new DeviceCollectDataCodecFactory()));
		
		// TODO config session.
//		acceptor.getSessionConfig()
		// acceptor have filterChain include TailFilter will invoke IoHandler for us to process our logic.
		acceptor.setHandler(ioHandler());

		if (properties.isAutostart()) {
			// acceptor bind will init acceptor start listen.
			if (serverBind(acceptor, properties)) {
				// TODO. post process acceptor.
			} else {
				// acceptor = null;
				logger.error("IO server bind error.");
			}
		} else {
			logger.info("IO server is need start by hand");
		}
		logger.trace("init bean IoAcceptor end...");
		return acceptor;
	}

	/**
	 * IO Server bind.
	 * 
	 * @param acceptor
	 *            IoAcceptor
	 * @param properties
	 *            IoServerProperties
	 * @return true if bind success
	 */
	private boolean serverBind(IoAcceptor acceptor, IoServerProperties properties) {
		try {
//			InetAddress inetAddress = InetAddress.getByName(ip);
			List<SocketAddress> addresses = resolveInetAddress(properties);
			if (CollectionUtils.isEmpty(addresses)) {
				logger.error("resolveInetAddress result: not exist one avalible address.");
				acceptor.dispose();
				return false;
			} else {
				acceptor.bind(addresses);
				logger.info("IO Server start binded addresses: {}", addresses);
			}
		} catch (IOException e) {
			logger.error("IoAcceptor bind error", e);
			return false;
		}
		return true;
	}
	
	/**
	 * resolve String to SocketAddress.
	 * eg: convert string 192.168.1.1:80,192.168.1.1:81 to a list of SocketAddress object.
	 * @param properties
	 *            IoServerProperties
	 * @return list of SocketAddress
	 */
	private List<SocketAddress> resolveInetAddress(IoServerProperties properties) {
		if (properties == null) {
			logger.error("IoServerProperties is null");
			return null;
		}
		String needParseAddressesStr = properties.getBindAddresses();
		logger.trace("resolveInetAddress start: origin addressesStr: {}", needParseAddressesStr);
		List<SocketAddress> addresses = null;
		if (StringUtils.isBlank(needParseAddressesStr) && validatePort(properties.getDefaultPort())) {
			if (properties.isUseAllLocalAddress()) {
				// TODO
//				NetworkInterface.getNetworkInterfaces()
			} else {
				addresses = new ArrayList<SocketAddress>(1);
				try {
					addresses.add(new InetSocketAddress(InetAddress.getLocalHost(), properties.getDefaultPort()));
					logger.debug("get local default ip address: {}", InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					logger.error("resolve local ip address error.", e);
					addresses = null;
				}
			}
			return addresses;
		} else {
			// TODO.
		}
		String[] addressesArr = needParseAddressesStr.split(CommonConstants.COMMA);
		addresses = new ArrayList<SocketAddress>(addressesArr.length);
		for (int i=0; i<addressesArr.length; i++) {
			String[] addressAndPort = addressesArr[i].split(CommonConstants.COLON);
			if (addressAndPort.length > 1) {
				// TODO validate ip and port.
				int port = NumberUtils.toInt(addressAndPort[1]);
				if (validatePort(port)) {
					InetSocketAddress socketAddress = new InetSocketAddress(addressAndPort[0], port);
					addresses.add(socketAddress);
				} else {
					logger.error("config address: {} is not avalible.", Arrays.toString(addressAndPort));
				}
			} else {
				if (validatePort(properties.getDefaultPort())) {
					InetSocketAddress socketAddress = new InetSocketAddress(addressAndPort[0], properties.getDefaultPort());
					addresses.add(socketAddress);
					logger.trace("address:{} not config port, use default port: {}", addressAndPort[0], properties.getDefaultPort());
				} else {
					logger.warn("address:{} not config port, default port: {} not avaliable", addressAndPort[0], properties.getDefaultPort());
				}
			}
		}
		if (CollectionUtils.isEmpty(addresses)) {
			logger.warn("resolve config string result that not exist one can used address, so use default local address and default port.");
			try {
				addresses.add(new InetSocketAddress(InetAddress.getLocalHost(), properties.getDefaultPort()));
				logger.debug("get local default ip address: {}", InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				logger.error("resolve local ip address error.", e);
				addresses = null;
			}
		}
		logger.trace("resolveInetAddress end...");
		return addresses;
	}
	
	/**
	 * validate port.
	 * 
	 * @param port
	 *            port.
	 * @return true if port is valid.
	 */
	private boolean validatePort(int port) {
		if (port < 1 || port > 65535) {
			return false;
		}
		// TODO. whether be occupied.
		return true;
	}
}
 