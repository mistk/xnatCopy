package my.util.handler;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractProcessor implements Processor {
	/**
	 * processor name.
	 */
	private String name;
	
	/**
	 * processor name default is class SimpleName.
	 */
	public AbstractProcessor() {
		name = getClass().getSimpleName();
	}
	public AbstractProcessor(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("processor name must be not empty");
		}
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Processor) {
			if (StringUtils.equals(getName(), ((Processor) obj).getName())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
}
