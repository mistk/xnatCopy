package my.actuate.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import my.spring.BaseBeanService;
import my.util.Msg;
import my.util.Response;

/**
 * service for BeansMvcEndpoint.
 * @author hubert
 */
public class BeansService extends BaseBeanService {
    /**
     * search limit bean count.
     */
    private Integer                                  searchBeanLimitCount = Integer.valueOf(20);
    /**
     * cached type name : subtypes info(map<beanName, beanInfo>).
     * 缓存: 一种类型和其所有子类信息.
     */
    private Map<Class<?>, List<Map<String, Object>>> typeMapToSubtype;
    private List<Class<?>>                           typeList;
    private List<Class<?>>                           additionalTypeList;
    @Autowired
    private BeansMvcEndpoint                         beansMvcEndpoint;



    Map<String, Object> buildMethodInfo(String pMethodName, String pBeanName, String pContextId) {
        if (StringUtils.isBlank(pMethodName) || StringUtils.isBlank(pBeanName) || StringUtils.isBlank(pContextId)) {
            log.warn("buildMethodInfo(): params not complete. methodName: {0}, beanName: {1}, contextId: {2}", pMethodName, pBeanName, pContextId);
            return Collections.emptyMap();
        }
        Map<String, Object> methodInfo = new HashMap<>();
        methodInfo.put("name", pMethodName);
        methodInfo.put("beanName", pBeanName);
        methodInfo.put("contextId", pContextId);
        ConfigurableApplicationContext context = getContext(pContextId);
        if (context == null) {
            log.error("buildMethodInfo(): not found Context by id: {0}", pContextId);
            return methodInfo;
        }
        methodInfo.put("beanUrl", buildBeanUrl(pBeanName, context));
        Class<?> beanClass = getBeanClass(context, pBeanName);
        if (beanClass == null) {
            log.error("buildMethodInfo(): not found Class for beanName: {0}", pBeanName);
            return methodInfo;
        }
        methodInfo.put("beanClassName", beanClass.getName());
        methodInfo.put("invokeUrl", beansMvcEndpoint.getBeansUrlPrefix() + BeansMvcEndpoint.URL_BEAN_INVOKE);
        return methodInfo;
    }



    Map<String, Object> buildPropertyInfo(String pPropName, String pBeanName, String pContextId) {
        if (StringUtils.isBlank(pPropName) || StringUtils.isBlank(pBeanName) || StringUtils.isBlank(pContextId)) {
            log.warn("params not complete. propName: {0}, beanName: {1}, contextId: {2}", pPropName, pBeanName, pContextId);
            return Collections.emptyMap();
        }
        Map<String, Object> propInfo = new HashMap<>();
        propInfo.put("name", pPropName);
        propInfo.put("beanName", pBeanName);
        propInfo.put("contextId", pContextId);
        ConfigurableApplicationContext context = getContext(pContextId);
        if (context == null) {
            log.error("buildPropertyInfo(): not found Context by id: {0}", pContextId);
            return propInfo;
        }
        Class<?> beanClass = getBeanClass(context, pBeanName);
        if (beanClass == null) {
            log.error("buildPropertyInfo(): not found Class for beanName: {0}", pBeanName);
            return propInfo;
        }
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(beanClass, pPropName);
        if (pd == null) {
            log.error("buildPropertyInfo(): not found property: {0} in class: {1.name}", pPropName, beanClass);
            return propInfo;
        }
        Object beanInstance = context.getBean(pBeanName);
        Map<String, Object> value = populatePropertyValueInfo(pd, beanInstance);

        propInfo.put("beanUrl", buildBeanUrl(pBeanName, context));
        propInfo.put("beanClassName", beanClass.getName());
        propInfo.put("type", pd.getPropertyType().getName());
        propInfo.put("writable", (pd.getWriteMethod() != null));
        propInfo.put("changeUrl", beansMvcEndpoint.getBeansUrlPrefix() + BeansMvcEndpoint.URL_BEAN_CHANGE);
        propInfo.put("value", value);
        return propInfo;
    }



    Map<String, Object> buildBeanInfo(String pBeanName, String pContextId) {
        if (StringUtils.isBlank(pBeanName)) {
            log.warn("buildBeanInfo(): beanName is empty", pBeanName);
            return Collections.emptyMap();
        }
        Map<String, Object> beanInfo = new LinkedHashMap<>();
        beanInfo.put("beanName", pBeanName);
        ConfigurableApplicationContext context = null;
        if (StringUtils.isBlank(pContextId)) {
            context = asConfigurableContext(getApplicationContext());
        } else {
            context = getContext(pContextId);
        }
        if (context == null) {
            log.error("buildBeanInfo(): not found Context by id: {0}", pContextId);
            return beanInfo;
        }
        beanInfo.put("contextId", context.getId());
        Class<?> beanClass = getBeanClass(context, pBeanName);
        if (beanClass == null) {
            log.error("buildBeanInfo(): can not retrive class for bean: {0}", pBeanName);
            return beanInfo;
        }
        Object beanInstance = context.getBean(pBeanName);
        // may be bean instance not exist.
        if (null == beanInstance) {
            log.debug("buildBeanInfo(): get bean instance is null from beanFactory. beanName: {0}", pBeanName);
        }
        beanInfo.put("url", buildBeanUrl(pBeanName, context));
        beanInfo.put("beanClassName", beanClass.getName());

        Map<String, Object> propertiesInfo = buildPropertiesInfo(beanClass, beanInstance, beanInfo);
        Map<String, Object> methodsInfo = buildMethodsInfo(beanClass, beanInstance, beanInfo);
        if (MapUtils.isNotEmpty(propertiesInfo)) {
            beanInfo.put("properties", propertiesInfo);
        }
        if (MapUtils.isNotEmpty(methodsInfo)) {
            beanInfo.put("methods", methodsInfo);
        }
        return beanInfo;
    }



    Response invokeMethod(String pMethodName, String pBeanName, String pContextId) {
        Response result = new Response(Boolean.FALSE);
        ConfigurableApplicationContext context = getContext(pContextId);
        Object beanInstance = context.getBean(pBeanName);
        if (beanInstance == null) {
            result.setErrorMsg(Msg.format("not found instance for bean: {0}", pBeanName));
            return result;
        }
        try {
            MethodUtils.invokeMethod(beanInstance, pMethodName);
            result.setSuccess(Boolean.TRUE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }



    Response changeProperty(String pPropName, String pBeanName, String pContextId, String pNewValue) {
        Response result = new Response(Boolean.FALSE);
        ConfigurableApplicationContext context = getContext(pContextId);
        Object beanInstance = context.getBean(pBeanName);
        if (beanInstance == null) {
            result.setErrorMsg(Msg.format("not found instance for bean: {0}", pBeanName));
            return result;
        }
        try {
            // use PropertyAccessor change Object's property.
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(beanInstance);
            beanWrapper.setPropertyValue(pPropName, pNewValue);
            result.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }



    List<Map<String, Object>> searchSubtype(String pClassName) {
        if (StringUtils.isBlank(pClassName)) {
            return Collections.emptyList();
        }
        Class<?> type = null;
        try {
            type = ClassUtils.forName(pClassName, getApplicationContext().getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            log.error(e, "searchSubtype():: {0}", pClassName);
            return Collections.emptyList();
        }
        return searchSubtype(type);
    }



    List<Map<String, Object>> searchSubtype(Class<?> pType) {
        if (pType == null) {
            return Collections.emptyList();
        }
        if (typeMapToSubtype == null) {
            typeMapToSubtype = new LinkedHashMap<>();
        }
        List<Map<String, Object>> subtypesInfo = typeMapToSubtype.get(pType);
        if (subtypesInfo == null) {
            subtypesInfo = new ArrayList<>();
            typeMapToSubtype.put(pType, subtypesInfo);
            for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
                ConfigurableApplicationContext context = it.next();
                String[] beanNames = context.getBeanNamesForType(pType);
                if (ArrayUtils.isEmpty(beanNames)) {
                    log.debug("not found subtype for type: {0.name} in context: {0.id}", pType, context);
                    continue;
                }
                for (String beanName : beanNames) {
                    Map<String, Object> beanInfo = new HashMap<>(2);
                    subtypesInfo.add(beanInfo);
                    beanInfo.put("beanName", beanName);
                    beanInfo.put("url", buildBeanUrl(beanName, context));
                }
                // sort by execute order.
                Collections.sort(subtypesInfo, AnnotationAwareOrderComparator.INSTANCE);
            }
        }
        return subtypesInfo;
    }
    
    
    /***
     * search matched name bean.
     * @param pBeanName
     *            search name.
     * @return matched beans.
     */
    Map<String, Object> searchBeans(String pBeanName) {
        if (StringUtils.isBlank(pBeanName)) {
            return Collections.emptyMap();
        }
        Pattern pattern = Pattern.compile(new StrBuilder().append(".*").append(pBeanName).append(".*").toString(), Pattern.CASE_INSENSITIVE);
        Map<String, Object> beans = new LinkedHashMap<>();
        int count = 0, limit = getSearchBeanLimitCount();
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); count < limit && it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            Iterator<String> beanNamesIt = beanFactory.getBeanNamesIterator();
            for (; count < limit && beanNamesIt.hasNext(); ) {
                String beanName = beanNamesIt.next();
                Matcher matcher = pattern.matcher(beanName);
                if (matcher.matches()) {
                    Map<String, Object> beanInfo = new HashMap<>();
                    beanInfo.put("url", buildBeanUrl(beanName, context));
                    beans.put(beanName, beanInfo);
                    count++;
                }
            }
        }
        return beans;
    }



    public void clearAllCache() {
        setTypeList(null);
        typeMapToSubtype = null;
    }



    private Class<?> getBeanClass(ConfigurableApplicationContext pContext, String pBeanName) {
        Class<?> beanClass = pContext.getType(pBeanName);
        if (beanClass == null) {
            ConfigurableListableBeanFactory beanFactory = pContext.getBeanFactory();
            log.warn(Msg.format("getBeanClass(): get from BeanFactory fail. beanName: {0}", pBeanName));
            BeanDefinition bd = beanFactory.getBeanDefinition(pBeanName);
            try {
                beanClass = org.springframework.util.ClassUtils.forName(bd.getBeanClassName(), beanFactory.getBeanClassLoader());
            } catch (ClassNotFoundException | LinkageError e) {
                log.error(e, "getBeanClass(): forClassName: {0.beanClassName} error", bd);
            }
            if (beanClass == null) {
                log.debug(Msg.format("getBeanClass(): get from BeanClassLoader fail. beanName: {0}", pBeanName));
                Object beanInstance = pContext.getBean(pBeanName);
                if (beanInstance != null) {
                    beanClass = beanInstance.getClass();
                }
            }
        }
        return beanClass;
    }

    Function<Method, Boolean> METHOD_FILTER = new Function<Method, Boolean>() {
        @Override
        public Boolean apply(Method pMethod) {
            return (pMethod.getDeclaringClass() == Object.class
                || pMethod.getDeclaringClass() == my.spring.BaseBean.class
                || pMethod.getDeclaringClass() == Collections.class
                || pMethod.getDeclaringClass() == Set.class
                || pMethod.getDeclaringClass() == List.class
                || pMethod.getDeclaringClass() == Map.class
                || pMethod.isBridge()
                || !Modifier.isPublic(pMethod.getModifiers())
                || Modifier.isStatic(pMethod.getModifiers())
                || Modifier.isAbstract(pMethod.getModifiers())
                || ArrayUtils.isNotEmpty(pMethod.getParameters())
                || BeanUtils.findPropertyForMethod(pMethod) != null); // filter getter and setter method.
        }
    };

    Function<Method, Map<String, Object>> METHOD_INFO_COLLECTOR = new Function<Method, Map<String,Object>>() {
        @Override
        public Map<String, Object> apply(Method pMethod) {
            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("returnType", pMethod.getReturnType().getName());
            methodInfo.put("declaringClass", pMethod.getDeclaringClass().getName());
            return methodInfo;
        }
    };


    private Map<String, Object> buildMethodsInfo(Class<?> pBeanClass, Object pBeanInstance, Map<String, Object> pBeanInfo) {
        if (pBeanClass == null) {
            return null;
        }
        Map<String, Object> methodsInfo = new HashMap<>();
        Method[] lMethods = ReflectionUtils.getAllDeclaredMethods(pBeanClass);
        for (Method method : lMethods) {
            if (METHOD_FILTER.apply(method)) {
                continue;
            }
            Map<String, Object> methodInfo = METHOD_INFO_COLLECTOR.apply(method);
            methodsInfo.put(method.getName(), methodInfo);
            String methodUrl = new StrBuilder()
                .append(pBeanInfo.get("url")).append("&")
                .append(BeansMvcEndpoint.PARAME_METHOD_NAME).append("=").append(method.getName())
                .toString();
            methodInfo.put("url", methodUrl);
        }
        return methodsInfo;
    }



    private Map<String, Object> buildPropertiesInfo(Class<?> pBeanClass, Object pBeanInstance, Map<String, Object> pBeanInfo) {
        Map<String, Object> propertiesInfo = new TreeMap<>();
        try {
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(pBeanClass);
            for (PropertyDescriptor pd : pds) {
                if (pd.getReadMethod() == null) {
                    continue;
                }
                String propName = pd.getName();
                Map<String, Object> propertyInfo = new HashMap<>();
                propertiesInfo.put(propName, propertyInfo);

                String propUrl = new StrBuilder()
                    .append(pBeanInfo.get("url")).append("&")
                    .append(BeansMvcEndpoint.PARAME_PROPERTY_NAME).append("=").append(propName)
                    .toString();
                Map<String, Object> value = populatePropertyValueInfo(pd, pBeanInstance);
                propertyInfo.put("url", propUrl);
                propertyInfo.put("type", pd.getPropertyType().getName());
                propertyInfo.put("value", value);
            }
        } catch (IllegalArgumentException e) {
            log.error(e, "buildPropertiesInfo() for class: {0.name}", pBeanClass);
        }
        return propertiesInfo;
    }



    private Map<String, Object> populatePropertyValueInfo(PropertyDescriptor pPropertyDescriptor, Object pBeanInstance) {
        Map<String, Object> valueAttr = new HashMap<>();
        Object originValue = null;
        String toStringPropName = "toString";
        valueAttr.put(toStringPropName, "");
        try {
            if (pBeanInstance != null) {
                originValue = pPropertyDescriptor.getReadMethod().invoke(pBeanInstance);
            }
            if (null != originValue) {
                if (originValue.getClass().isArray()) {
                    valueAttr.put(toStringPropName, ToStringBuilder.reflectionToString(originValue));
                } else {
                    valueAttr.put(toStringPropName, originValue.toString());
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error(e, "populatePropertyValue() error");
        }
        boolean found = false;
        if (!ClassUtils.isPrimitiveOrWrapper(pPropertyDescriptor.getPropertyType())
            && !String.class.isAssignableFrom(pPropertyDescriptor.getPropertyType())
            && Class.class.isAssignableFrom(pPropertyDescriptor.getPropertyType())) {
            for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); !found && it.hasNext();) {
                ConfigurableApplicationContext context = it.next();
                ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                String[] existBeanNames = beanFactory.getBeanNamesForType(pPropertyDescriptor.getPropertyType());
                if (ArrayUtils.isEmpty(existBeanNames)) {
                    continue;
                }
                for (String beanName : existBeanNames) {
                    if (originValue == beanFactory.getBean(beanName)) {
                        valueAttr.put("beanName", beanName);
                        valueAttr.put("beanUrl", buildBeanUrl(beanName, context));
                        valueAttr.put(toStringPropName, originValue.toString());
                        found = true;
                        break;
                    }
                }
            }
        }
        valueAttr.put("beanRef", found);
        return valueAttr;
    }



    private String buildBeanUrl(String pBeanName, ApplicationContext pContext) {
        String beanUrl = new StrBuilder()
            .append(beansMvcEndpoint.getBeansUrlPrefix())
            .append("/")
            .append(pBeanName)
            .append("?").append(BeansMvcEndpoint.PARAME_CONTEXTID).append("=").append(pContext.getId())
            .toString();
        return beanUrl;
    }



    private ConfigurableApplicationContext getContext(String pContextId) {
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
            if (StringUtils.equals(context.getId(), pContextId)) {
                return context;
            }
        }
        return null;
    }



    /**
     * get all associated ConfigurableApplicationContext.
     * @return set of ConfigurableApplicationContext
     */
    private Set<ConfigurableApplicationContext> getContextHierarchy() {
        Set<ConfigurableApplicationContext> contexts = new LinkedHashSet<ConfigurableApplicationContext>();
        ApplicationContext context = getApplicationContext();
        while (context != null) {
            contexts.add(asConfigurableContext(context));
            context = context.getParent();
        }
        return contexts;
    }



    /**
     * cast ApplicationContext to ConfigurableApplicationContext.
     * @param applicationContext
     *            ApplicationContext
     * @return ConfigurableApplicationContext
     */
    private ConfigurableApplicationContext asConfigurableContext(ApplicationContext applicationContext) {
        Assert.isTrue(applicationContext instanceof ConfigurableApplicationContext,
            "'" + applicationContext + "' does not implement ConfigurableApplicationContext");
        return (ConfigurableApplicationContext) applicationContext;
    }



    /**
     * show type list.
     * @return
     */
    public List<Class<?>> getTypeList() {
        if (CollectionUtils.isNotEmpty(typeList)) {
            return typeList;
        }
        typeList = new ArrayList<Class<?>>();
//        typeList.add(SpringApplication.class);
//        typeList.add(ApplicationContext.class);
//        typeList.add(ApplicationContextInitializer.class);
        typeList.add(ApplicationListener.class);
        typeList.add(BeanPostProcessor.class);
        typeList.add(BeanFactoryPostProcessor.class);
        if (CollectionUtils.isNotEmpty(getAdditionalTypeList())) {
            typeList.addAll(getAdditionalTypeList());
        }
        return typeList;
    }



    /**
     * @return the searchBeanLimitCount
     */
    public Integer getSearchBeanLimitCount() {
        return searchBeanLimitCount;
    }



    /**
     * @param pAdditionalTypeList
     *            the additionalTypeList to set
     */
    public void setAdditionalTypeList(List<Class<?>> pAdditionalTypeList) {
        additionalTypeList = pAdditionalTypeList;
        setTypeList(null);
    }



    /**
     * @param pSearchBeanLimitCount
     *            the searchBeanLimitCount to set
     */
    public void setSearchBeanLimitCount(Integer pSearchBeanLimitCount) {
        searchBeanLimitCount = pSearchBeanLimitCount;
    }



    /**
     * @return the additionalTypeList
     */
    public List<Class<?>> getAdditionalTypeList() {
        return additionalTypeList;
    }



    /**
     * @param pTypeList
     *            the typeList to set
     */
    public void setTypeList(List<Class<?>> pTypeList) {
        typeList = pTypeList;
    }

}
