package my.actuate.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.ManagementServletContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import my.util.Log;

public class BeansMvcEndpoint extends AbstractNamedMvcEndpoint implements ApplicationContextAware {
    static final String              PARAME_ACTION           = "action";
    static final String              PARAME_CONTEXTID        = "contextId";
    static final String              PARAME_PROPERTY_NAME    = "propertyName";
    static final String              PARAME_BEAN_NAME        = "beanName";
    static final String              PARAME_NEWVALUE         = "newValue";
    static final String              PARAME_TYPE_NAME        = "typeName";
    private static final String      URL_BEAN_CHANGE         = "bean/change";
    private final Log                log                     = Log.of(getClass());
    private String                   beanIdentifierSeparator = "||";
    private ApplicationContext       applicationContext;
    @Autowired
    private ManagementServletContext managementServletContext;
    @Autowired
    private BeansService             beansService;



    public BeansMvcEndpoint() {
        super("admin/beans", "/admin/beans", false);
    }



    @GetMapping(path = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView browse(@RequestParam Map<String, Object> pParams) {
        String action = (String) pParams.get(PARAME_ACTION);
        log.trace("action: {0}", action);
        ModelAndView modelAndView = new ModelAndView();
        String viewName = "index";
        if ("searchBeans".equals(action)) {
            String beanName = (String) pParams.get(PARAME_BEAN_NAME);
            log.debug("searchBeans beanName: {0}", beanName);
            Map<String, Object> beans = beansService.searchBeans(beanName);
            modelAndView.addObject("beans", beans)
                .addObject("limitCount", beansService.getSearchBeanLimitCount())
                .addObject("kw", beanName);
            viewName = "search";
        } else if ("getSubtypes".equals(action)) {
            String typeName = (String) pParams.get(PARAME_TYPE_NAME);
            log.debug("getSubtypes typeName: {0}", typeName);
            List<Map<String, Object>> subTypes = beansService.searchSubtype(typeName);
            modelAndView.addObject("subtypes", subTypes);
            viewName = "fragments :: subtypes";
        } else {
            List<Class<?>> typeList = beansService.getTypeList();
            List<Map<String, Object>> subTypes = beansService.searchSubtype(typeList.get(0).getName());
            modelAndView.addObject("types", typeList)
                .addObject("subtypes", subTypes);
            viewName = "index";
        }
        modelAndView.setViewName(builViewName(viewName));
        return modelAndView;
    }



    @GetMapping(path = {"/{beanName:.*}"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView browse(@PathVariable("beanName") String pBeanName, @RequestParam Map<String, Object> pParams) {
        String action = (String) pParams.get(PARAME_ACTION);
        log.debug("beanName: {0}, action: {1}", pBeanName, action);
        ModelAndView modelAndView = new ModelAndView();
        String viewName = "index";
        if ("change".equals(action))  {
            
        } else {
            String contextId = (String) pParams.get(PARAME_CONTEXTID);
            String propertyName = (String) pParams.get(PARAME_PROPERTY_NAME);
            log.debug("contextId: {0}, propertyName: {1}", contextId, propertyName);
            Map<String, Object> beanInfo = buildBeanInfo(pBeanName, contextId, propertyName);
            modelAndView.addAllObjects(beanInfo);
            viewName = "bean";
        }
        modelAndView.setViewName(builViewName(viewName));
        return modelAndView;
    }



    @ResponseBody
    @PostMapping(path = {URL_BEAN_CHANGE})
    public Map<String, Object> change(@RequestParam Map<String, Object> pParams) {
        String contextId = (String) pParams.get(PARAME_CONTEXTID);
        String propertyName = (String) pParams.get(PARAME_PROPERTY_NAME);
        String beanName = (String) pParams.get(PARAME_BEAN_NAME);
        String newValue = (String) pParams.get(PARAME_NEWVALUE);
        Map<String, Object> response = new HashMap<>();
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
            if (!StringUtils.equals(contextId, context.getId())) {
                continue;
            }
            String[] beanNames = context.getBeanDefinitionNames();
            for (String name : beanNames) {
                if (!StringUtils.equals(beanName, name)) {
                    continue;
                }
                Object beanInstance = context.getBean(beanName);
                Class<?> beanClass = context.getType(beanName);
                if (beanClass == null) {
                    break;
                }
                try {
                    BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                    for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                        String propName = pd.getName();
                        if (!StringUtils.equals(propertyName, propName)) {
                            continue;
                        }
                        Object originValue = null;
                        if (beanInstance != null) {
                            originValue = pd.getReadMethod().invoke(beanInstance);
                        }
                        if (ClassUtils.isPrimitiveOrWrapper(pd.getPropertyType()) || String.class.isAssignableFrom(pd.getPropertyType())) {
                            // add spring bean property editor
                            PropertyEditor propertyEditor = PropertyEditorManager.findEditor(pd.getPropertyType());
                            propertyEditor.setAsText(newValue);
                            pd.getWriteMethod().invoke(beanInstance, propertyEditor.getValue());
                        } else {
                            // TODO other type.
                        }
                    }
                } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    response.put("succss", false);
                    response.put("errorMsg", e.getMessage());
                }
            }
        }
        response.put("succss", true);
        return response;
    }



    private String builViewName(String viewSuffix) {
//        String contextPath = managementServletContext.getContextPath();
        StrBuilder sb = new StrBuilder();
//        if (!contextPath.endsWith("/")) {
//            sb.append("/");
//        }
        sb.append("admin/beans/").append(viewSuffix);
        return sb.toString();

    }



    /**
     * build beans info.
     * @param pBeanName
     *            bean name.
     * @return beans info.
     */
    private Map<String, Object> buildBeanInfo(String pBeanName, String pContextId, String pSpecifiedProperty) {
        if (StringUtils.isBlank(pBeanName)) {
            return Collections.emptyMap();
        }
        Map<String, Object> beanInfo = new LinkedHashMap<>();
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
            if (!StringUtils.equals(pContextId, context.getId())) {
                continue;
            }
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            String[] beanNames = beanFactory.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                if (!StringUtils.equals(beanName, pBeanName)) {
                    continue;
                }
                BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                if (!isBeanEligible(beanName, bd, beanFactory)) {
                    continue;
                }
                Object beanInstance = beanFactory.getBean(beanName);
                if (null == beanInstance) {
                    log.debug("get bean: {0} is null from beanFactory.", beanName);
                }
                Class<?> beanClass = beanFactory.getType(beanName);
                // may be bean instance not exist.
                // beanClass = beanInstance.getClass();
                // try {
                // beanClass = ClassUtils.forName(bd.getBeanClassName(),
                // beanFactory.getBeanClassLoader());
                // } catch (ClassNotFoundException | LinkageError e) {
                // String errorMsg = MessageFormat.format("forClassName: {}
                // error", bd.getBeanClassName());
                // logger.error(errorMsg, e);
                // continue;
                // }
                beanInfo.put("beanName", beanName);
                beanInfo.put("contextId", pContextId);
                StrBuilder beanUrl = new StrBuilder().append(beanName).append("?").append(PARAME_CONTEXTID).append("=").append(context.getId());
                beanInfo.put("url", beanUrl);

                // bean common info.
                beanInfo.put("beanClassName", beanClass.getName());
                if (StringUtils.isBlank(pSpecifiedProperty)) {
                    Map<String, Object> propertiesInfo = buildPropertiesInfo(beanClass, beanInstance, beanInfo);
                    if (MapUtils.isNotEmpty(propertiesInfo)) {
                        beanInfo.put("properties", propertiesInfo);
                    }
                } else {
                    Map<String, Object> propertyInfo = buildPropertyInfo(pSpecifiedProperty, beanClass, beanInstance, beanInfo);
                    if (MapUtils.isNotEmpty(propertyInfo)) {
                        beanInfo.put("property", propertyInfo);
                    }
                }
                Map<String, Object> methodsInfo = buildMethodsInfo(beanClass);
                if (MapUtils.isNotEmpty(methodsInfo)) {
                    beanInfo.put("methods", methodsInfo);
                }
                break;
            }
        }
        return beanInfo;
    }



    private String buildBeanIdentifier(ApplicationContext pApplicationContext, String pBeanName) {
        return new StrBuilder().append(pApplicationContext.getId())
            .append(getBeanIdentifierSeparator())
            .append(pBeanName)
            .toString();
    }



    protected boolean isBeanEligible(String beanName, BeanDefinition bd, ConfigurableBeanFactory bf) {
        return (bd.getRole() != BeanDefinition.ROLE_INFRASTRUCTURE && (!bd.isLazyInit() || bf.containsSingleton(beanName)));
    }



    private Set<ConfigurableApplicationContext> getContextHierarchy() {
        Set<ConfigurableApplicationContext> contexts = new LinkedHashSet<ConfigurableApplicationContext>();
        ApplicationContext context = this.applicationContext;
        while (context != null) {
            contexts.add(asConfigurableContext(context));
            context = context.getParent();
        }
        return contexts;
    }



    private Map<String, Object> buildMethodsInfo(Class<?> pBeanClass) {
        if (pBeanClass == null) {
            return null;
        }
        Map<String, Object> methodsInfo = new HashMap<>();
        Method[] lMethods = ReflectionUtils.getAllDeclaredMethods(pBeanClass);
        for (Method method : lMethods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            Map<String, Object> methodInfo = new HashMap<>();
            methodsInfo.put(method.getName(), methodInfo);
            methodInfo.put("returnType", method.getReturnType().getName());
            methodInfo.put("declaringClass", method.getDeclaringClass().getName());
        }
        return methodsInfo;
    }



    private Map<String, Object> buildPropertyInfo(String pSpecifiedProperty, Class<?> pBeanClass, Object pBeanObj, Map<String, Object> pBeanInfo) {
        Map<String, Object> propInfo = new HashMap<>();
        try {
            BeanInfo lBeanInfo = Introspector.getBeanInfo(pBeanClass);
            PropertyDescriptor[] pds = lBeanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getReadMethod() == null) {
                    continue;
                }
                String propName = pd.getName();
                if (!StringUtils.equals(pSpecifiedProperty, propName)) {
                    continue;
                }
                propInfo.put("name", propName);
                propInfo.put("type", pd.getPropertyType().getName());
                // String propUrl = new StrBuilder()
                // .append(pBeanInfo.get("")).append("&")
                // .append(PARAMETER_PROPERTY_NAME).append("=").append(propName)
                // .toString();
                // propInfo.put("url", propUrl);
                propInfo.put("writable", (pd.getWriteMethod() != null));
//                propInfo.put("changeUrl", new StrBuilder().append("../").append(URL_BEAN_CHANGE).toString());
                propInfo.put("beanInfo", pBeanInfo);
                if (pBeanObj == null) {
                    log.warn("get value for property: {0} from bean, but bean object is null", pd.getName());
                } else {
                    Map<String, Object> value = populatePropertyValue(pd, pBeanObj);
                    propInfo.put("value", value);
                }
            }
        } catch (IntrospectionException e) {
            log.error(e, "buildPropertyInfo for class: {0.name}", pBeanClass);
        }
        return propInfo;
    }



    private Map<String, Object> buildPropertiesInfo(Class<?> pBeanClass, Object pBeanObj, Map<String, Object> pBeanInfo) {
        if (pBeanClass == null) {
            return null;
        }
        Map<String, Object> propertiesInfo = new TreeMap<>();
        try {
            BeanInfo lBeanInfo = Introspector.getBeanInfo(pBeanClass);
            PropertyDescriptor[] pds = lBeanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getReadMethod() == null) {
                    continue;
                }
                String propName = pd.getName();
                Map<String, Object> propertyInfo = new HashMap<>();
                propertiesInfo.put(propName, propertyInfo);
                propertyInfo.put("type", pd.getPropertyType().getName());
                String propUrl = new StrBuilder()
                    .append(pBeanInfo.get("url")).append("&")
                    .append(PARAME_PROPERTY_NAME).append("=").append(propName)
                    .toString();
                propertyInfo.put("url", propUrl);
                if (pBeanObj == null) {
                    log.warn("get value for property: {0.name} from bean, but bean object is null", pd);
                } else {
                    Map<String, Object> value = populatePropertyValue(pd, pBeanObj);
                    propertyInfo.put("value", value);
                }
            }
        } catch (IntrospectionException | IllegalArgumentException e) {
            log.error(e, "buildPropertiesInfo for class: {0.name}", pBeanClass);
        }
        return propertiesInfo;
    }



    private Map<String, Object> populatePropertyValue(PropertyDescriptor pPropertyDescriptor, Object pBeanObj) {
        Map<String, Object> valueAttr = new HashMap<>();
        Object originValue = null;
        String toStringPropName = "toString";
        valueAttr.put(toStringPropName, "");
        try {
            originValue = pPropertyDescriptor.getReadMethod().invoke(pBeanObj);
            if (null != originValue) {
                valueAttr.put(toStringPropName, originValue.toString());
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error(e, "PropertyDescriptor get value error!");
            return valueAttr;
        }
        boolean found = false;
        if (!ClassUtils.isPrimitiveOrWrapper(pPropertyDescriptor.getPropertyType()) && !String.class
            .isAssignableFrom(pPropertyDescriptor.getPropertyType()) && Class.class.isAssignableFrom(pPropertyDescriptor.getPropertyType())) {
            // searchBeans(pPropertyDescriptor.getName());
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
                        StrBuilder beanUrl = new StrBuilder().append(beanName).append("?").append(PARAME_CONTEXTID).append("=")
                            .append(context.getId());
                        valueAttr.put("beanUrl", beanUrl);
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



    private ConfigurableApplicationContext asConfigurableContext(ApplicationContext applicationContext) {
        Assert.isTrue(applicationContext instanceof ConfigurableApplicationContext,
            "'" + applicationContext + "' does not implement ConfigurableApplicationContext");
        return (ConfigurableApplicationContext) applicationContext;
    }



    @Override
    public void setApplicationContext(ApplicationContext pApplicationContext) throws BeansException {
        applicationContext = pApplicationContext;
    }



    public String getBeanIdentifierSeparator() {
        return beanIdentifierSeparator;
    }



    public void setBeanIdentifierSeparator(String pBeanIdentifierSeparator) {
        beanIdentifierSeparator = pBeanIdentifierSeparator;
    }

}
