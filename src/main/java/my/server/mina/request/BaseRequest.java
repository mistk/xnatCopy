package my.server.mina.request;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * BaseRequest
 * @author xnat
 *
 */
public class BaseRequest implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8039399861607968256L;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
