package my.autoconfigure;

import org.apache.mina.filter.logging.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ioServer")
public class IoServerProperties {
	/**
	 * whether IO server auto start.
	 */
	private boolean autostart = true;
	/**
	 * formated: ip:port,ip:port 
	 * eg: 192.168.1.1:80,192.168.1.1:81
	 */
	private String bindAddresses;
	/**
	 * use All local address. default is false.
	 */
	private boolean useAllLocalAddress;
	/**
	 * default port is 10000. Generally used start server or development mode.
	 */
	private int defaultPort = 10000;
	/**
	 * FilterLogConfig.
	 */
	private FilterLogConfig filterLogConfig;
	

	/**
	 * ref org.apache.mina.filter.logging.LoggingFilter.
	 * @author xnat
	 *
	 */
	class FilterLogConfig {
	    /** The log level for the exceptionCaught event. Default to WARN. */
	    private LogLevel exceptionCaughtLevel = LogLevel.WARN;
	    /** The log level for the messageSent event. Default to INFO. */
	    private LogLevel messageSentLevel = LogLevel.INFO;
	    /** The log level for the messageReceived event. Default to INFO. */
	    private LogLevel messageReceivedLevel = LogLevel.INFO;
	    /** The log level for the sessionCreated event. Default to INFO. */
	    private LogLevel sessionCreatedLevel = LogLevel.INFO;
	    /** The log level for the sessionOpened event. Default to INFO. */
	    private LogLevel sessionOpenedLevel = LogLevel.INFO;
	    /** The log level for the sessionIdle event. Default to INFO. */
	    private LogLevel sessionIdleLevel = LogLevel.INFO;
	    /** The log level for the sessionClosed event. Default to INFO. */
	    private LogLevel sessionClosedLevel = LogLevel.INFO;
		public LogLevel getExceptionCaughtLevel() {
			return exceptionCaughtLevel;
		}
		public void setExceptionCaughtLevel(LogLevel exceptionCaughtLevel) {
			this.exceptionCaughtLevel = exceptionCaughtLevel;
		}
		public LogLevel getMessageSentLevel() {
			return messageSentLevel;
		}
		public void setMessageSentLevel(LogLevel messageSentLevel) {
			this.messageSentLevel = messageSentLevel;
		}
		public LogLevel getMessageReceivedLevel() {
			return messageReceivedLevel;
		}
		public void setMessageReceivedLevel(LogLevel messageReceivedLevel) {
			this.messageReceivedLevel = messageReceivedLevel;
		}
		public LogLevel getSessionCreatedLevel() {
			return sessionCreatedLevel;
		}
		public void setSessionCreatedLevel(LogLevel sessionCreatedLevel) {
			this.sessionCreatedLevel = sessionCreatedLevel;
		}
		public LogLevel getSessionOpenedLevel() {
			return sessionOpenedLevel;
		}
		public void setSessionOpenedLevel(LogLevel sessionOpenedLevel) {
			this.sessionOpenedLevel = sessionOpenedLevel;
		}
		public LogLevel getSessionIdleLevel() {
			return sessionIdleLevel;
		}
		public void setSessionIdleLevel(LogLevel sessionIdleLevel) {
			this.sessionIdleLevel = sessionIdleLevel;
		}
		public LogLevel getSessionClosedLevel() {
			return sessionClosedLevel;
		}
		public void setSessionClosedLevel(LogLevel sessionClosedLevel) {
			this.sessionClosedLevel = sessionClosedLevel;
		}
	}
//	class FilterLogConfig {
//		private String exceptionCaughtLevel;
//	    private String messageSentLevel;
//	    private String messageReceivedLevel;
//	    private String sessionCreatedLevel;
//	    private String sessionOpenedLevel;
//	    private String sessionIdleLevel;
//	    private String sessionClosedLevel;
//		public String getExceptionCaughtLevel() {
//			return exceptionCaughtLevel;
//		}
//		public void setExceptionCaughtLevel(String exceptionCaughtLevel) {
//			this.exceptionCaughtLevel = exceptionCaughtLevel;
//		}
//		public String getMessageSentLevel() {
//			return messageSentLevel;
//		}
//		public void setMessageSentLevel(String messageSentLevel) {
//			this.messageSentLevel = messageSentLevel;
//		}
//		public String getMessageReceivedLevel() {
//			return messageReceivedLevel;
//		}
//		public void setMessageReceivedLevel(String messageReceivedLevel) {
//			this.messageReceivedLevel = messageReceivedLevel;
//		}
//		public String getSessionCreatedLevel() {
//			return sessionCreatedLevel;
//		}
//		public void setSessionCreatedLevel(String sessionCreatedLevel) {
//			this.sessionCreatedLevel = sessionCreatedLevel;
//		}
//		public String getSessionOpenedLevel() {
//			return sessionOpenedLevel;
//		}
//		public void setSessionOpenedLevel(String sessionOpenedLevel) {
//			this.sessionOpenedLevel = sessionOpenedLevel;
//		}
//		public String getSessionIdleLevel() {
//			return sessionIdleLevel;
//		}
//		public void setSessionIdleLevel(String sessionIdleLevel) {
//			this.sessionIdleLevel = sessionIdleLevel;
//		}
//		public String getSessionClosedLevel() {
//			return sessionClosedLevel;
//		}
//		public void setSessionClosedLevel(String sessionClosedLevel) {
//			this.sessionClosedLevel = sessionClosedLevel;
//		}
//	}
	
	public boolean isUseAllLocalAddress() {
		return useAllLocalAddress;
	}

	public void setUseAllLocalAddress(boolean useAllLocalAddress) {
		this.useAllLocalAddress = useAllLocalAddress;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public void setDefaultPort(int defaultPort) {
		this.defaultPort = defaultPort;
	}

	public String getBindAddresses() {
		return bindAddresses;
	}

	public void setBindAddresses(String bindAddresses) {
		this.bindAddresses = bindAddresses;
	}

	public boolean isAutostart() {
		return autostart;
	}

	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	public FilterLogConfig getFilterLogConfig() {
		return filterLogConfig;
	}

	public void setFilterLogConfig(FilterLogConfig filterLogConfig) {
		this.filterLogConfig = filterLogConfig;
	}

}
