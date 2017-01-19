package my.util.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import my.util.KeyConstants;

/**
 * simple get property from GeneratorContext or Object in GeneratorContext.
 * support simple convert value to special other value if necessary.
 * 
 * return type  expression
 * String       prop=[abcd]
 * must be configure propertyMaping.
 * @author hubert
 */
public class SimpleMultiFieldGenerator extends AbstractDTOGenerator<Map<String, Object>> implements MultiFieldGenerator {
	/**
	 * property:expression(eg: Object.property or Object.property.property)
	 */
	private Map<String, String> propertyMaping;
	/**
	 * convert function separator.
	 */
	private String convertFunctionSeparator = ":";
	/**
	 * field value converter.
	 */
	protected final Converter converter;
    /**
     * constant configure string. like: [acd]{String}
     */
	protected static final Pattern PATTERN_CONSTANT = Pattern.compile("^\\[(.+)\\](\\{[a-zA-Z]+\\})?$");
	/**
	 * like: [aaa]
	 */
	protected static final Pattern PATTERN_BRACKET = Pattern.compile("\\[(.+)\\]");
	/**
	 * like: {aaa}
	 */
	protected static final Pattern PATTERN_BRACE = Pattern.compile("\\{(.+)\\}");
	/**
	 * like: funcName or funcName(arg1,arg2)
	 */
	protected static final Pattern PATTERN_FUNCTION = Pattern.compile("^([a-zA-Z]+)(\\([a-zA-Z0-9]+(,[a-zA-Z0-9])*\\))?$");
	/**
	 * configure string like: Object.prop1[0].prop2{functions}
	 */
	protected static final Pattern PATTERN_CONFIG_STR = Pattern.compile("^([a-zA-Z.0-9\\[\\]]+)(\\{.+\\})?$");
	
	protected SimpleMultiFieldGenerator() {
	    converter = new Converter();
    }
	
	@Override
	protected boolean isValid(GeneratorContext pGeneratorContext) {
		boolean valid = super.isValid(pGeneratorContext);
		if (valid) {
			if (MapUtils.isEmpty(propertyMaping)) {
				logger.warn("propertyMaping is empty, ignore this generator");
				valid = false;
			}
		}
		if (valid) {
		    if (converter == null) {
		        logger.error("lose a available converter...");
		        valid = false;
		    }
		}
		return valid;
	}
	
	@Override
	protected Map<String, Object> doGenerate(GeneratorContext pGeneratorContext) {
	    logger.trace("generate multi field start...");
		Map<String, Object> result = new HashMap<>();
		Iterator<Entry<String, String>> it = propertyMaping.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			try {
			    Object value = populateValue(entry, pGeneratorContext);
			    result.put(entry.getKey(), value);
			} catch (Exception e) {
			    logger.error("populate value error!!!!", e);
            }
		}
		logger.debug("generate multi field result: {}", result);
		return result;
	}

	
    /**
     * populate value for configure entry.
     * 
     * @param pConfigEntry
     *            configure entry.
     * @param pGeneratorContext
     *            GeneratorContext
     * @return result value.
     */
	protected Object populateValue(Entry<String, String> pConfigEntry, GeneratorContext pGeneratorContext) {
	    logger.trace("populate value for config entry: {}={}", pConfigEntry.getKey(), pConfigEntry.getValue());
		Object resultObj = null;
		String configStr = pConfigEntry.getValue();
		if (StringUtils.isBlank(configStr)) {
		    logger.error("config entry: {}={} is not format right.", pConfigEntry.getKey(), pConfigEntry.getValue());
		    return null;
		}
		// judge whether a constant value.
		Matcher matcher = PATTERN_BRACKET.matcher(configStr);
		boolean isConstantConfigStr = false;
		if (matcher.find()) {
		    resultObj = matcher.group(1);
		    isConstantConfigStr = true;
		} else {
		    resultObj = findValueFromContext(pConfigEntry, pGeneratorContext);
		}
		matcher = PATTERN_BRACE.matcher(configStr);
		// string like : [acd] or Object.prop{func} need convert.
		if (isConstantConfigStr || matcher.find()) {
		    ConverterContext converterContext = new ConverterContext();
		    converterContext.put(ConverterConstants.CONFIG_ENTRY, pConfigEntry);
		    converterContext.put(ConverterConstants.RESULT_OBJECT, resultObj);
		    resultObj = converter.convert(converterContext);
		    logger.debug("config entry: {}={}, converted value: {}", pConfigEntry.getKey(), pConfigEntry.getValue(), resultObj);
		}
		return resultObj;
	}
	

    private Object findValueFromContext(Entry<String, String> pConfigEntry, GeneratorContext pGeneratorContext) {
        logger.trace("find value from context for config entry: {}={}", pConfigEntry.getKey(), pConfigEntry.getValue());
        String configStr = pConfigEntry.getValue();
        if (StringUtils.isBlank(configStr)) {
            logger.error("entry: {}={}, config string is empty.", pConfigEntry.getKey(), pConfigEntry.getValue());
            return null;
        }
        Matcher matcher = PATTERN_CONFIG_STR.matcher(configStr);
        // like: object.prop1[0].prop2
        String fieldsStr = null;
        if (matcher.find() && matcher.groupCount() > 0) {
            fieldsStr = matcher.group(1);
        } else {
            logger.error("entry: {}={}, config string may be not format right.", pConfigEntry.getKey(), pConfigEntry.getValue());
            return null;
        }
        Object resultValue = null;
        String[] fieldArr = fieldsStr.split(".");
        for (String fieldStr : fieldArr) {
//            resultValue = getValueByField(fieldStr);
            
        }
        return resultValue;
    }


    /**
     * convert chain will process object that get from GeneratorContext.
     * added common convert method.
     * Converter, sub class should be add custom function for convert value,
     * then should re-new sub class for my.util.dto.SimpleMultiFieldGenerator.converter
     * 
     * @author hubert
     */
	protected class Converter {
	    // TODO add cache. for performance and show all already exist convert methods, when development.
//	    Map<String, Method> cachedMethodMaping = new HashMap<>();

        public Object convert(ConverterContext pContext) {
            Entry<String, String> configEntry = pContext.get(ConverterConstants.CONFIG_ENTRY);
            Object resultObj = pContext.get(ConverterConstants.RESULT_OBJECT);
            logger.debug("convert start, entry: {}={}, result object: {}", configEntry.getKey(), configEntry.getValue(), resultObj);
            // not need to confirm whether necessary judge resultObj is null.
            // different convert method should process resultObject by self.
//            if (resultObj == null) {
//                return null;
//            }
            List<String> functionNames = getFunctionNames(configEntry);
            logger.trace("get functionNames: {} from config entry: {}={}", functionNames, configEntry.getKey(), configEntry.getValue());
            if (CollectionUtils.isEmpty(functionNames)) {
                logger.debug("config entry: {}={} not parse out any available function name.", configEntry.getKey(), configEntry.getValue());
                return resultObj;
            }

            List<Method> convertMethods = getConvertMethods(functionNames);
            if (CollectionUtils.isEmpty(convertMethods)) {
                logger.error("not found exist convert methods by function names: {}", functionNames);
                return null;
            }
            try {
                for (Method method : convertMethods) {
                    method.setAccessible(true);
                    resultObj = method.invoke(this, pContext);
                    pContext.put(ConverterConstants.RESULT_OBJECT, resultObj);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                String errorMsg = MessageFormat.format("convert config entry: {}={}", configEntry.getKey(), configEntry.getValue());
                logger.error(errorMsg, e);
            }
            return resultObj;
        }


        /**
         * get list of convert method by function names.
         * 
         * @param pFunctionNames
         *            list of function name.
         * @return list of convert method.
         */
        private List<Method> getConvertMethods(List<String> pFunctionNames) {
            if (CollectionUtils.isEmpty(pFunctionNames)) {
                logger.warn("find convert methods, but function names is empty");
                return null;
            }
            // TODO get cached.
            List<Method> methods = new ArrayList<>(pFunctionNames.size());
            for (String funcName: pFunctionNames) {
                Method method = getConvertMethod(funcName);
                if (method  == null) {
                    logger.error("");
                } else {
                    methods.add(method);
                }
            }
            return methods;
        }


        /**
         * get convert method by function name.
         * 
         * @param pFunctionName
         *            function name.
         * @return convert method
         */
        private Method getConvertMethod(String pFunctionName) {
        	if (StringUtils.isEmpty(pFunctionName)) {
        		logger.debug("find convert method, paramter function name is empty");
        		return null;
        	}
        	for (Class<?> cls = getClass(); cls != null; cls = cls.getSuperclass()) {
        		for (Method method: cls.getDeclaredMethods()) {
        			ConvertMethod convertMethod = method.getDeclaredAnnotation(ConvertMethod.class);
                    if (convertMethod == null) {
                        continue;
                    }
                    if (ArrayUtils.isEmpty(convertMethod.value())) {
                    	if (StringUtils.equals(pFunctionName, method.getName())) {
                    		return method;
                    	}
                    } else {
                        String[] functionNames = convertMethod.value();
                        for (String functionName : functionNames) {
                            if (StringUtils.isBlank(functionName)) {
                                 logger.error("convert method: {} config name must be not blank.", method);
                                 continue;
                            }
                            if (StringUtils.equals(pFunctionName, functionName)) {
                            	return method;
                            }
                        }
                    }
                }
            }
        	return null;
        }


        /**
         * get function names from a configure entry.
         * 
         * @param pConfigEntry
         *            configure entry.
         * @return list of function name
         */
        private List<String> getFunctionNames(Entry<String, String> pConfigEntry) {
        	logger.trace("get function names start, config entry: {}={}", pConfigEntry.getKey(), pConfigEntry.getValue());
        	if (StringUtils.isBlank(pConfigEntry.getValue())) {
        		logger.debug("configEntry: {}={} not exist any function need act.", pConfigEntry.getKey(), pConfigEntry.getValue());
        		return null;
        	}
        	// like funcName1(arg1,arg2);funcName2
        	String functionsConfigStr = "";
        	Matcher matcher = PATTERN_BRACE.matcher(pConfigEntry.getValue());
        	if (matcher.find() && matcher.groupCount() > 0) {
        		functionsConfigStr = matcher.group(1);
        	} else {
        		logger.warn("configEntry: {}={} not exist available function name.", pConfigEntry.getKey(), pConfigEntry.getValue());
        		return null;
        	}
        	String[] functionsConfigArr = functionsConfigStr.split(SimpleMultiFieldGenerator.this.getConvertFunctionSeparator());
        	List<String> functionNames = new ArrayList<>(functionsConfigArr.length);
        	for (String functionConfigStr : functionsConfigArr) {
        		matcher = PATTERN_FUNCTION.matcher(functionConfigStr);
        		if (matcher.find() && matcher.groupCount() > 0) {
        			String functionName = matcher.group(1);
        			functionNames.add(functionName.trim());
        		} else {
        			logger.error("functionConfigStr:{} not format right.", functionsConfigStr);
        		}
        	}
        	return functionNames;
        }


        @ConvertMethod
        protected Object func1(ConverterContext pContext) {
        	System.out.println("parent");
            return null;
        }
    }
	
	/**
	 * ConverterConstants
	 * @author hubert
	 * @param <T>
	 */
	static class ConverterConstants<T> extends KeyConstants<T> {
        public ConverterConstants(String pName) {
            super(pName);
        }
        final static ConverterConstants<Entry<String, String>> CONFIG_ENTRY = new ConverterConstants<>("CONFIG_ENTRY");
        final static ConverterConstants<Object> RESULT_OBJECT = new ConverterConstants<>("RESULT_OBJECT");
    }
	class ConverterContext {
        private Map<Object, Object> context = new HashMap<>();
        @SuppressWarnings("unchecked")
        <T> T get(ConverterConstants<T> pKey) {
            return (T) getContext().get(pKey);
        }
        @SuppressWarnings("unchecked")
        <T> T put(ConverterConstants<T> pKey, Object pValue) {
            return (T) getContext().put(pKey, pValue);
        }
        public Map<Object, Object> getContext() {
            return context;
        }
    }
	 /**
     * stand for a convert method.
     * @author hubert
     *
     */
    @Target(ElementType.METHOD)
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface ConvertMethod {
        /**
         * convert function name.
         */
        String[] value() default {};
    }
    
    
	public Map<String, String> getPropertyMaping() {
		return propertyMaping;
	}

	public void setPropertyMaping(Map<String, String> pPropertyMaping) {
		propertyMaping = pPropertyMaping;
	}

	public String getConvertFunctionSeparator() {
		return convertFunctionSeparator;
	}

	public void setConvertFunctionSeparator(String pConvertFunctionSeparator) {
		convertFunctionSeparator = pConvertFunctionSeparator;
	}
}
