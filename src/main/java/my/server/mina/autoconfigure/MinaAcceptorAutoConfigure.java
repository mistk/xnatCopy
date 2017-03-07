package my.server.mina.autoconfigure;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import my.constanst.CommonConstants;
import my.server.mina.codec.DeviceCollectDataCodecFactory;
import my.server.mina.handler.DeviceCollectDataIoHandler;
import my.server.mina.handler.RequestDataProcessor;
import my.server.mina.handler.ResponseProcessor;
import my.server.mina.handler.ValidateProcessor;

@Configuration
@EnableConfigurationProperties(MinaServerProperties.class)
@ConditionalOnProperty(prefix = MinaServerProperties.PREFIX, name = "enabled", matchIfMissing = true, havingValue = "true")
@ConditionalOnClass(IoAcceptor.class)
public class MinaAcceptorAutoConfigure {
    private static final String FILTER_NAME_LOGGER  = "logger";
    private static final String FILTER_NAME_CODEC   = "codec";
    private static final String MINA_IOACCEPTOR     = "mina.ioAcceptor";
    private static final String MINA_IOHANDLER      = "mina.ioHandler";
    private static final String MINA_LOGGING_FILTER = "mina.loggingFilter";
    private final Logger        logger              = LoggerFactory.getLogger(getClass());



    @Bean(MINA_LOGGING_FILTER)
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = MinaServerProperties.PREFIX + ".filterLogConfig")
    public LoggingFilter loggingFilter() {
        logger.trace("Initializ bean {} start...", MINA_LOGGING_FILTER);
        LoggingFilter loggingFilter = new LoggingFilter();
        logger.trace("Initializ bean {} end...", MINA_LOGGING_FILTER);
        return loggingFilter;
    }



    @Bean(MINA_IOHANDLER)
    @ConditionalOnMissingBean
    public IoHandler ioHandler() {
        logger.trace("Initializ bean {} start...", MINA_IOHANDLER);
        // TODO custom handler.
        IoHandler ioHandler = new DeviceCollectDataIoHandler();
        // IoHandlerAdapter/ChainedIoHandler
        logger.trace("Initializ bean {} end...", MINA_IOHANDLER);
        return ioHandler;
    }

    

    @Bean(MINA_IOACCEPTOR)
    @ConditionalOnMissingBean
    public IoAcceptor ioAcceptor(MinaServerProperties properties) {
        logger.trace("Initializ bean :{} start...", MINA_IOACCEPTOR);
        IoAcceptor acceptor = new NioSocketAcceptor();
        // SimpleIoProcessorPool include some session processor, every processor
        // process session.
        // org.apache.mina.core.polling.AbstractPollingIoProcessor.process(S)
        // invoke filters.
        acceptor.getFilterChain().addLast(FILTER_NAME_LOGGER, loggingFilter());
        // acceptor.getFilterChain().addLast(FILTER_NAME_CODEC, new
        // ProtocolCodecFilter(new
        // TextLineCodecFactory(Charset.forName("UTF-8"))));
        acceptor.getFilterChain().addLast(FILTER_NAME_CODEC, new ProtocolCodecFilter(new DeviceCollectDataCodecFactory()));

        // TODO config session.
        // acceptor.getSessionConfig()
        // acceptor have filterChain include TailFilter will invoke IoHandler
        // for us to process our logic.
        acceptor.setHandler(ioHandler());

        if (properties.isAutostart()) {
            // acceptor bind will init acceptor start listen.
            if (serverBind(acceptor, properties)) {
                // TODO. post process acceptor.
            } else {
                // acceptor = null;
                logger.error("Mina server bind error.");
            }
        } else {
            logger.info("Mina server is need start by hand");
        }
        logger.trace("Initializ bean {} end...", MINA_IOACCEPTOR);
        return acceptor;
    }



    @Bean(RequestDataProcessor.COMPONENT_NAME)
    RequestDataProcessor requestDataProcessor() {
        return new RequestDataProcessor();
    }



    @Bean(ResponseProcessor.COMPONENT_NAME)
    ResponseProcessor responseProcessor() {
        return new ResponseProcessor();
    }



    @Bean(ValidateProcessor.COMPONENT_NAME)
    ValidateProcessor validateProcessor() {
        return new ValidateProcessor();
    }



    /**
     * IO Server bind.
     * @param acceptor
     *            IoAcceptor
     * @param properties
     *            MinaServerProperties
     * @return true if bind success
     */
    private boolean serverBind(IoAcceptor acceptor, MinaServerProperties properties) {
        try {
            // InetAddress inetAddress = InetAddress.getByName(ip);
            List<SocketAddress> addresses = resolveInetAddress(properties);
            if (CollectionUtils.isEmpty(addresses)) {
                logger.error("resolveInetAddress result: not exist one avalible address.");
                acceptor.dispose();
                return false;
            } else {
                acceptor.bind(addresses);
                logger.info("Mina Server start binded addresses: {}", addresses);
            }
        } catch (IOException e) {
            logger.error("IoAcceptor bind error", e);
            return false;
        }
        return true;
    }



    /**
     * resolve String to SocketAddress. eg: convert string
     * 192.168.1.1:80,192.168.1.1:81 to a list of SocketAddress object.
     * @param properties
     *            MinaServerProperties
     * @return list of SocketAddress
     */
    private List<SocketAddress> resolveInetAddress(MinaServerProperties properties) {
        if (properties == null) {
            logger.error("MinaServerProperties is null");
            return null;
        }
        String needParseAddressesStr = properties.getBindAddresses();
        logger.trace("resolveInetAddress start: origin addressesStr: {}", needParseAddressesStr);
        List<SocketAddress> addresses = null;
        if (StringUtils.isBlank(needParseAddressesStr) && validatePort(properties.getDefaultPort())) {
            if (properties.isUseAllLocalAddress()) {
                // TODO
                // NetworkInterface.getNetworkInterfaces()
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
        for (int i = 0; i < addressesArr.length; i++) {
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
                    InetSocketAddress socketAddress = new InetSocketAddress(addressAndPort[0],
                        properties.getDefaultPort());
                    addresses.add(socketAddress);
                    logger.trace("address:{} not config port, use default port: {}", addressAndPort[0],
                        properties.getDefaultPort());
                } else {
                    logger.warn("address:{} not config port, default port: {} not avaliable", addressAndPort[0],
                        properties.getDefaultPort());
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
