package my.util.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * base DTO.
 * @author hubert
 */
public class BaseDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
