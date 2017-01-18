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
import java.util.Arrays;
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
	 * like: {funcName} or {funcName(arg1,arg2)}
	 */
	protected static final Pattern PATTERN_FUNCTION = Pattern.compile("^\\{([a-zA-Z]+)(\\([a-zA-Z0-9]+(,[a-zA-Z0-9])*\\))?\\}$");
	
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
		Map<String, Object> result = new HashMap<>();
		Iterator<Entry<String, String>> it = propertyMaping.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			Object value = createValue(entry, pGeneratorContext);
			result.put(entry.getKey(), value);
		}
		return result;
	}

	private Object createValue(Entry<String, String> pEntry, GeneratorContext pGeneratorContext) {
		Object target = null;
		String configStr = pEntry.getValue();
		Matcher matcher = PATTERN_CONSTANT.matcher(configStr);
		if (matcher.find()) {
			target = createConstantValue(pEntry, matcher);
		} else {
			
		}
		return target;
	}
	
	private Object createConstantValue(Entry<String, String> pEntry, Matcher pMatcher) {
		Object result = null;
		String configStr = pEntry.getValue();
//		Matcher matcher = PATTERN_CONSTANT.matcher(configStr);
		// Get something inside brackets.
		Matcher mather = PATTERN_BRACKET.matcher(configStr);
		String literalStr = mather.group(1);
		mather = PATTERN_BRACE.matcher(configStr);
		if (mather.find()) {
			String typeStr = mather.group(1);
		} else {
			
		}
		return result;
	}
	
//	public static void main(String[] args) {
//		Map<String, Method> map = new SubConverter().getConvertMethodMaping();
//		System.out.println(map);
//	}
	
//	static class SubConverter extends Converter {
//		@Override
//        protected Object func1(ConverterContext pContext) {
//			System.out.println("sub");
//            return null;
//        }
//		@ConvertMethod
//		protected Object func2(ConverterContext pContext) {
//			return null;
//		}
//	}
	
	/**
	 * Converter, sub class should be add custom function for convert value.
	 * @author hubert
	 */
	protected class Converter {
        private final Map<String, Method> convertMethodMaping = new HashMap<>();
        public Converter() {
            init();
//            logger.debug("init Converter, convertFuncMaping: {}", convertMethodMaping);
        }
        private void init() {
        	List<Method> methods = new ArrayList<>();
        	for (Class<?> cls = getClass(); cls != null; cls = cls.getSuperclass()) {
        		methods.addAll(Arrays.asList(cls.getDeclaredMethods()));
        	}
            for (Method method : methods) {
            	ConvertMethod convertFn = method.getDeclaredAnnotation(ConvertMethod.class);
                if (convertFn == null) {
                    continue;
                }
                if (ArrayUtils.isEmpty(convertFn.value())) {
                	putMethod(method.getName(), method);
                } else {
                    String[] names = convertFn.value();
                    for (String name : names) {
                        if (StringUtils.isBlank(name)) {
                            // log
                        } else {
                        	putMethod(method.getName(), method);
                        }
                    }
                }
            }
        }
        
        private void putMethod(String pFuncName, Method pMethod) {
        	if (convertMethodMaping.containsKey(pFuncName)) {
//        		logger.warn("init Converter, function name: {} exist, override.", pFuncName);
        	}
        	convertMethodMaping.put(pFuncName, pMethod);
        }
        private List<Method> getConvertMethods(List<String> pFunctionNames) {
        	List<Method> methods = null;
        	methods = new ArrayList<>();
        	return methods;
        }
        
        
        public Object convert(ConverterContext pContext) {
            Entry<String, String> configEntry = pContext.get(ConverterConstants.CONFIG_ENTRY);
            logger.trace("convert start..., entry: ", configEntry.getKey(), configEntry.getValue());
            List<String> functionNames = getFunctionNames(configEntry);
            logger.trace("get functionNames: {} from config entry: {}={}", functionNames, configEntry.getKey(), configEntry.getValue());
            if (CollectionUtils.isEmpty(functionNames)) {
            	logger.warn("config entry: {}={} not parse out any available function name.", configEntry.getKey(), configEntry.getValue());
            	return null;
            }
            Object result = null;
            // TODO get function list.
            getConvertMethods(functionNames);
            List<Method> convertMethods = new ArrayList<>();
            try {
                for (Method method : convertMethods) {
                    method.setAccessible(true);
                    result = method.invoke(this, pContext);
                    pContext.put(ConverterConstants.PRE_CONVERT_RESULT, result);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            	String errorMsg = MessageFormat.format("convert config entry: {}={}", configEntry.getKey(), configEntry.getValue());
//                logger.error(errorMsg, e);
            }
            return result;
        }
        
        /**
		 * get function names from a configure entry.
		 * 
		 * @param pConfigEntry
		 *            configure entry.
		 * @return list of function name
		 */
        private List<String> getFunctionNames(Entry<String, String> pConfigEntry) {
        	logger.trace("get function names from config entry: {}={}", pConfigEntry.getKey(), pConfigEntry.getValue());
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

        public Map<String, Method> getConvertMethodMaping() {
			return convertMethodMaping;
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
        final static ConverterConstants<Entry<String, String>> CONFIG_ENTRY = new ConverterConstants<>("configEntry");
        final static ConverterConstants<Object> PRE_CONVERT_RESULT = new ConverterConstants<>("preConvertResult");
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
