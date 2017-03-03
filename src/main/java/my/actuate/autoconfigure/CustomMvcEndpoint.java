package my.actuate.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.endpoint.mvc.ManagementServletContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import my.actuate.beans.BeansMvcEndpoint;

@ManagementContextConfiguration
@ConditionalOnWebApplication
public class CustomMvcEndpoint {

    @Bean
//    @ConditionalOnEnabledEndpoint("dyn/beans")
    public BeansMvcEndpoint beansMvcEndpoint(ManagementServletContext managementServletContext) {
        BeansMvcEndpoint beansMvcEndpoint = new BeansMvcEndpoint(managementServletContext);
        return beansMvcEndpoint;
    }
}
