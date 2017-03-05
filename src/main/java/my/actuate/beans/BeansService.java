package my.actuate.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import my.spring.BaseBeanService;

@Component
@ConfigurationProperties(prefix = "endpoints.admin.beans")
class BeansService extends BaseBeanService {
    /**
     * search limit bean count.
     */
    private Integer searchBeanLimitCount = Integer.valueOf(20);
    /**
     * cached type name : subtypes info(map<beanName, beanInfo>).
     */
    private Map<Class<?>, List<Map<String, Object>>> typeMapToSubtype;
    private List<Class<?>> typeList;
    private List<Class<?>> additionalTypeList;

    @Override
    protected void postConstruct() {
        super.postConstruct();
    }

    //AutoProxyUtils.determineTargetClass(this.applicationContext.getBeanFactory(), beanName);
//    @Async
    @EventListener(ContextStartedEvent.class)
    private void initBeansInfoCache(ContextStartedEvent pContextStartedEvent) {
    }

    /**
     * 
     * @param pClassName
     * @return
     */
    public List<Map<String, Object>> searchSubtype(String pClassName) {
        if (StringUtils.isBlank(pClassName)) {
            return Collections.emptyList();
        }
        Class<?> type = null;
        try {
            type = ClassUtils.forName(pClassName, getApplicationContext().getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            log.error(e, "search subtypes for class: {0}", pClassName);
            return Collections.emptyList();
        }
        return searchSubtype(type);
    }
    
    public List<Map<String, Object>> searchSubtype(Class<?> pType) {
        if (pType == null) {
            return Collections.emptyList();
        }
        if (MapUtils.isEmpty(typeMapToSubtype)) {
            typeMapToSubtype = new LinkedHashMap<>();
        }
        List<Map<String, Object>> subtypes = typeMapToSubtype.get(pType);
        if (subtypes == null) {
            subtypes = new ArrayList<>();
            typeMapToSubtype.put(pType, subtypes);
            for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
                ConfigurableApplicationContext context = it.next();
                String[] beanNames = context.getBeanNamesForType(pType);
                if (ArrayUtils.isEmpty(beanNames)) {
                    continue;
                }
                List<Map<String, Object>> beansInfo = typeMapToSubtype.get(pType);
                if (CollectionUtils.isEmpty(beansInfo)) {
                    beansInfo = new ArrayList<>();
                    typeMapToSubtype.put(pType, beansInfo);
                }
                for (String beanName : beanNames) {
                    Map<String, Object> beanInfo = new HashMap<>(2);
                    beansInfo.add(beanInfo);
                    String beanUrl = new StrBuilder()
                        .append("../bean/")
                        .append(beanName)
                        .append("?").append(BeansMvcEndpoint.PARAME_CONTEXTID).append("=").append(context.getId())
                        .toString();
                    beanInfo.put("beanName", beanName);
                    beanInfo.put("url", beanUrl);
                }
                // sort by execute order.
                Collections.sort(beansInfo, AnnotationAwareOrderComparator.INSTANCE);
            }
        }
        return subtypes;
    }
    
    
//    private Object doWithContext(Function<ConfigurableApplicationContext, Object> pContextCallback) {
//        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
//            ConfigurableApplicationContext context = it.next();
//            return pContextCallback.apply(context);
//        }
//    }
    

    
    public Map<Class<?>, List<Map<String, Object>>> getTypeMapToSubtype() {
        if (MapUtils.isNotEmpty(typeMapToSubtype)) {
            return typeMapToSubtype;
        }
        typeMapToSubtype = new HashMap<>();
        List<Class<?>> lTypeList = getTypeList();
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
            for (Class<?> type : lTypeList) {
                String[] beanNames = context.getBeanNamesForType(type);
                if (ArrayUtils.isEmpty(beanNames)) {
                    continue;
                }
                List<Map<String, Object>> beansInfo = typeMapToSubtype.get(type);
                if (CollectionUtils.isEmpty(beansInfo)) {
                    beansInfo = new ArrayList<>();
                    typeMapToSubtype.put(type, beansInfo);
                }
                for (String beanName : beanNames) {
                    Map<String, Object> beanInfo = new HashMap<>(2);
                    
                    beansInfo.add(beanInfo);
                    String beanUrl = new StrBuilder()
                        .append("../bean/")
                        .append(beanName)
                        .append("?").append(BeansMvcEndpoint.PARAME_CONTEXTID).append("=").append(context.getId())
                        .toString();
                    beanInfo.put("beanName", beanName);
                    beanInfo.put("url", beanUrl);
                }
                // sort by execute order.
                Collections.sort(beansInfo, AnnotationAwareOrderComparator.INSTANCE);
            }
        }
        return typeMapToSubtype;
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
        Pattern pattern = Pattern.compile(new StrBuilder().append(".*").append(pBeanName).append(".*").toString());
        Map<String, Object> beans = new LinkedHashMap<>();
        int count = 0, limit = getSearchBeanLimitCount();
        for (Iterator<ConfigurableApplicationContext> it = getContextHierarchy().iterator(); count <= limit && it.hasNext();) {
            ConfigurableApplicationContext context = it.next();
//            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            String[] beanNames = context.getBeanDefinitionNames();
            for (int i = 0; i < beanNames.length && count <= limit; i++) {
                Matcher matcher = pattern.matcher(beanNames[i]);
                if (matcher.matches()) {
                    count++;
                    Map<String, Object> beanInfo = new HashMap<>();
                    String beanUrl = new StrBuilder()
                        .append("beans/")
                        .append(beanNames[i])
                        .append("?").append(BeansMvcEndpoint.PARAME_CONTEXTID).append("=").append(context.getId())
                        .toString();
                    beanInfo.put("url", beanUrl);
                    beans.put(beanNames[i], beanInfo);
                }
            }
        }
        return beans;
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
        typeList.add(ApplicationContextInitializer.class);
        typeList.add(ApplicationListener.class);
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
     * @param pTypeList the typeList to set
     */
    public void setTypeList(List<Class<?>> pTypeList) {
        typeList = pTypeList;
    }

    /**
     * @param pAdditionalTypeList the additionalTypeList to set
     */
    public void setAdditionalTypeList(List<Class<?>> pAdditionalTypeList) {
        additionalTypeList = pAdditionalTypeList;
    }
}
