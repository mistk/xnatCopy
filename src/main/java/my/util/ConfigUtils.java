package my.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import my.constanst.CommonConstants;

/**
 * utils about configure
 * @author xnat
 *
 */
public class ConfigUtils {
	/**
	 * resolve String to SocketAddress
	 * 
	 * @param addressesStr
	 *            eg: 192.168.1.1:80,192.168.1.1:81
	 * @return list of SocketAddress
	 */
	public static List<SocketAddress> resolveInetAddress(String addressesStr) {
		if (StringUtils.isBlank(addressesStr)) {
			return null;
		}
		List<SocketAddress> addresses = null;
		String[] addressesArr = addressesStr.split(CommonConstants.COMMA);
		addresses = new ArrayList<SocketAddress>(addressesArr.length);
		for (int i=0; i<addressesArr.length; i++) {
			String[] addressAndPort = addressesArr[i].split(CommonConstants.COLON);
			if (addressAndPort.length > 1) {
				InetSocketAddress socketAddress = new InetSocketAddress(addressAndPort[0], NumberUtils.toInt(addressAndPort[1]));
				addresses.add(socketAddress);
			}
		}
		return addresses;
	}
}
