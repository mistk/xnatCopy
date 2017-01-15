package my.util.dto;

import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is a template which generate any kind of value and return it
 * @author hubert
 *
 * @param <T>
 */
public abstract class AbstractDTOGenerator<T> implements DTOGenerator<T> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Enable or disable this generator. Usually it is used for feature
	 * controlling.
	 */
	private boolean enabled = true;

	/**
	 * Generate any Object and return it. If isEnabled() == false. Return null.
	 * If isValid() == false return null.
	 * 
	 * @param pGeneratorContext
	 *            . It contains the information needed to generate the value.
	 */
	@Override
	public T generate(GeneratorContext pGeneratorContext) {
		if (!isEnabled()) {
			return null;
		}
		if (!isValid(pGeneratorContext)) {
			return null;
		}
		T dto = getCachedValue(pGeneratorContext);
		// debugDTO(dto);
		return dto;
	}
	
	/**
     * get cache value by key.
     * @param pContext
     *            generator context
     * @return cached DTO
     */
	protected T getCachedValue(GeneratorContext pContext) {
		return doGenerate(pContext);
	}

	/**
	 * doGenerate DTO by context.
	 * 
	 * @param pGeneratorContext
	 *            context
	 * @return DTO
	 */
	protected abstract T doGenerate(GeneratorContext pGeneratorContext);

	/**
	 * Validate the current context. Return false if fails
	 * 
	 * @param generatorContext
	 *            GeneratorContext
	 * @return
	 */
	protected boolean isValid(GeneratorContext generatorContext) {
		boolean valid = true;
		return valid;
	}

	/**
	 * check whether params exists in pGeneratorContext.
	 * 
	 * @param pGeneratorContext
	 *            generatorContext
	 * @param pDtoConstants
	 *            dto constants.
	 * @return valid
	 */
	protected boolean validateObjectInsideContext(GeneratorContext generatorContext, DTOConstants<?>... pDtoConstants) {
		if (generatorContext == null || pDtoConstants == null) {
			return true;
		}
		boolean isValid = true;
		for (DTOConstants<?> dtoConstant : pDtoConstants) {
			if (generatorContext.get(dtoConstant) == null) {
				isValid = false;
				logger.error(new StrBuilder().append("validObjectInsideContext(): ").append(dtoConstant.getName())
						.append(" isn't inside the context.").toString());
				break;
			}
		}
		return isValid;
	}
	
	@Override
	public String toString() {
		return getClass().getName();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
