package my.actuate.beans;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.ManagementServletContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import my.util.Log;
import my.util.Response;

public class BeansMvcEndpoint extends AbstractNamedMvcEndpoint implements InitializingBean {
    static final String              PARAME_ACTION           = "action";
    static final String              PARAME_CONTEXTID        = "contextId";
    static final String              PARAME_PROPERTY_NAME    = "propertyName";
    static final String              PARAME_METHOD_NAME      = "methodName";
    static final String              PARAME_BEAN_NAME        = "beanName";
    static final String              PARAME_NEWVALUE         = "newValue";
    static final String              PARAME_TYPE_NAME        = "typeName";
    static final String              URL_BEAN_CHANGE         = "/change";
    static final String              URL_BEAN_INVOKE         = "/invoke";
    private final Log                log                     = Log.of(getClass());
    private String                   beansUrlPrefix;
    @Autowired
    private ManagementServletContext managementServletContext;
    @Autowired
    private BeansService             beansService;



    public BeansMvcEndpoint() {
        super("admin/beans", "/admin/beans", false);
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        StrBuilder sb = new StrBuilder();
        String contextPath = managementServletContext.getContextPath();
        sb.append(contextPath).append(getPath());
        setBeansUrlPrefix(sb.toString());
    }

    // AutoProxyUtils.determineTargetClass(this.applicationContext.getBeanFactory(),
    // beanName);
    // @Async
//    @EventListener(ContextStartedEvent.class)
//    private void init(ContextStartedEvent pContextStartedEvent) {
//        StrBuilder sb = new StrBuilder();
//        String contextPath = managementServletContext.getContextPath();
//        sb.append(contextPath).append(getPath());
//        setBeanUrlPrefix(sb.toString());
//    }

    @ExceptionHandler
    String expHandler(Exception pEx, HttpServletRequest pReq) {
        log.error(pEx, "BeansMvcEndpoint out error!!");
        pReq.setAttribute("ex", pEx);
        return "error";
    }

    @GetMapping(path = {"", "/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView browse(@RequestParam Map<String, Object> pParams) {
        String action = (String) pParams.get(PARAME_ACTION);
        log.trace("browse(): action: {0}", action);
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
        viewName = builViewName(viewName);
        log.trace("view name: {0}", viewName);
        modelAndView.setViewName(viewName);
        return modelAndView;
    }



    @GetMapping(path = {"/{beanName:.*}"}, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView browse(@PathVariable("beanName") String pBeanName, @RequestParam Map<String, Object> pParams) {
        log.debug("browse(): beanName: {0}, params: {1}", pBeanName, pParams);
        ModelAndView modelAndView = new ModelAndView();
        String contextId = (String) pParams.get(PARAME_CONTEXTID);
        String propertyName = (String) pParams.get(PARAME_PROPERTY_NAME);
        String methodName = (String) pParams.get(PARAME_METHOD_NAME);
        String viewName = null;
        if (StringUtils.isNotBlank(propertyName)) {
            viewName = "property";
            Map<String, Object> propInfo = beansService.buildPropertyInfo(propertyName, pBeanName, contextId);
            modelAndView.addAllObjects(propInfo);
        } else if (StringUtils.isNotBlank(methodName)) {
            Map<String, Object> methodInfo = beansService.buildMethodInfo(methodName, pBeanName, contextId);
            modelAndView.addAllObjects(methodInfo);
            viewName = "method";
        } else {
            Map<String, Object> beanInfo = beansService.buildBeanInfo(pBeanName, contextId);
            modelAndView.addAllObjects(beanInfo);
            viewName = "bean";
        }
        viewName = builViewName(viewName);
        modelAndView.setViewName(viewName);
        return modelAndView;
    }



    @ResponseBody
    @PostMapping(path = {URL_BEAN_CHANGE})
    public Response change(@RequestParam Map<String, Object> pParams) {
        log.debug("change(): params: {0}", pParams);
        String contextId = (String) pParams.get(PARAME_CONTEXTID);
        String propertyName = (String) pParams.get(PARAME_PROPERTY_NAME);
        String beanName = (String) pParams.get(PARAME_BEAN_NAME);
        String newValue = (String) pParams.get(PARAME_NEWVALUE);
        Response result = beansService.changeProperty(propertyName, beanName, contextId, newValue);
        return result;
    }



    @ResponseBody
    @PostMapping(path = {URL_BEAN_INVOKE})
    public Response invoke(@RequestParam Map<String, Object> pParams) {
        log.debug("invoke(): params: {0}", pParams);
        String methodName = (String) pParams.get(PARAME_METHOD_NAME);
        String beanName = (String) pParams.get(PARAME_BEAN_NAME);
        String contextId = (String) pParams.get(PARAME_CONTEXTID);
        Response result = beansService.invokeMethod(methodName, beanName, contextId);
        return result;
    }




    private String builViewName(String viewSuffix) {
        // String contextPath = managementServletContext.getContextPath();
        StrBuilder sb = new StrBuilder();
        // if (!contextPath.endsWith("/")) {
        // sb.append("/");
        // }
        sb.append("admin/beans/").append(viewSuffix);
        return sb.toString();

    }



    public String getBeansUrlPrefix() {
        return beansUrlPrefix;
    }



    private void setBeansUrlPrefix(String pBeanUrlPrefix) {
        beansUrlPrefix = pBeanUrlPrefix;
    }
}
