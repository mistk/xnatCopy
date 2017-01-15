package my.util.dto;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * This class is used to generate a DTO This class defines mapping between
 * field(setter method) and generators. Each generator will return an object and
 * set to the field of DTO Supporting nest property like a.b.c and or update
 * several fields at one time.
 * @author hubert
 * @param <T>
 */
public class DynamicDTOGenerator<T> extends AbstractDTOGenerator<T> {
    /**
     * The class type to create an instance for this DTOManager.
     */
    private Class<T>                     DTOClass;
    /**
     * DTO fields (setter method) and generator mapping.
     */
    private Map<String, DTOGenerator<?>> fieldGenerators = new LinkedHashMap<>();

    @Override
    protected boolean isValid(GeneratorContext pGeneratorContext) {
        boolean valid = super.isValid(pGeneratorContext);
        if (getDTOClass() == null) {
                logger.error("isValid(): Property DTOClass is null and DTO instance is not in context. DTOClass is "
                        + getDTOClass() + " generatorContext is " + pGeneratorContext);
            valid = false;
        }
        if (MapUtils.isEmpty(fieldGenerators)) {
        	logger.error("fieldGenerators is empty!");
        }
        return valid;
    }

	@Override
	protected T doGenerate(GeneratorContext pGeneratorContext) {
		T targetObj = createTargetInstance(pGeneratorContext);
		if (targetObj == null) {
			logger.error("generator: {} create corresponding target class: {} not success", getClass().getCanonicalName(), getDTOClass().getName());
			return null;
		}
		Iterator<Entry<String, DTOGenerator<?>>> it = fieldGenerators.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, DTOGenerator<?>> entry = it.next();
			DTOGenerator<?> generator = entry.getValue();
			if (generator == null) {
				logger.error("exist null generator.");
				continue;
			}
			if (generator instanceof MultiFieldGenerator) {
				Map<String, Object> propertyValues = ((MultiFieldGenerator) generator).generate(pGeneratorContext);
				logger.debug("generator: {} generate propertyValues: {}", generator, propertyValues);
				try {
					BeanUtils.populate(targetObj, propertyValues);
				} catch (IllegalAccessException | InvocationTargetException e) {
					String errorMsg = MessageFormat.format("populate propertyValues: {0} for bean: {2}", propertyValues, targetObj);
					logger.error(errorMsg, e);
				}
				continue;
			}
			String propertyName = entry.getKey();
			if (StringUtils.isBlank(propertyName)) {
				logger.error("exist blank property name. please check config");
				continue;
			}
			Object value = generator.generate(pGeneratorContext);
			logger.debug("generator: {} generate value: {}", generator, value);
			try {
				BeanUtils.setProperty(targetObj, propertyName, value);
			} catch (IllegalAccessException | InvocationTargetException e) {
				String errorMsg = MessageFormat.format("set property: {0} value: {1} for bean: {2}", propertyName, value, targetObj);
				logger.error(errorMsg, e);
			}
		}
		return targetObj;
	}
	
	/**
	 * create instance for DTOClass.
	 * 
	 * @param pGeneratorContext
	 *            GeneratorContext
	 * @return target instance.
	 */
	@SuppressWarnings("unchecked")
	protected T createTargetInstance(GeneratorContext pGeneratorContext) {
		T targetObj = null;
		Class<T> targetClass = getDTOClass();
		try {
			if (Map.class.equals(targetClass)) {
				targetObj = (T) new LinkedHashMap<>();
			} else if (Set.class.equals(targetClass)) {
				targetObj = (T) new LinkedHashSet<>();
			} else if (List.class.equals(targetClass)) {
				targetObj = (T) new ArrayList<>();
			} else if (BaseDTO.class.isAssignableFrom(targetClass)) {
				targetObj = targetClass.newInstance();
			} else {
				logger.error("target class: {} is not support by: {}", targetClass.getName(), DynamicDTOGenerator.class.getSimpleName());
			}
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("create instance error", e);
		}
		return targetObj;
	}
	
	public Class<T> getDTOClass() {
		return DTOClass;
	}
	
	public void setDTOClass(Class<T> pDTOClass) {
		DTOClass = pDTOClass;
	}
}
