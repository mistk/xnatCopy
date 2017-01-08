package my.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ioServer")
public class IoServerProperties {
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

}
