package my.util.dto;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

/**
 * simple get property from GeneratorContext or Object in GeneratorContext.
 * convert value to special other value if necessary, eg: 
 * 1<->true, 0<->false, "true"<->true, true<->false and so on.
 * must be configure propertyMaping.
 * @author hubert
 */
public class SimpleMultiFieldGenerator extends AbstractDTOGenerator<Map<String, Object>> implements MultiFieldGenerator {
	/**
	 * property:expression(eg: Object.property or Object.property.property)
	 */
	private Map<String, String> propertyMaping;
	
	@Override
	protected boolean isValid(GeneratorContext pGeneratorContext) {
		boolean valid = super.isValid(pGeneratorContext);
		if (valid) {
			if (MapUtils.isEmpty(propertyMaping)) {
				logger.warn("propertyMaping is empty, ignore this generator");
				valid = false;
			}
		}
		return valid;
	}
	
	@Override
	protected Map<String, Object> doGenerate(GeneratorContext pGeneratorContext) {
		
		return null;
	}

}
