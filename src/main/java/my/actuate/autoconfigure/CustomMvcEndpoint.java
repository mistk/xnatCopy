package my.actuate.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcHypermediaManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import my.actuate.beans.BeansMvcEndpoint;
import my.actuate.beans.BeansService;

@ManagementContextConfiguration
@ConditionalOnWebApplication
@AutoConfigureAfter(EndpointWebMvcHypermediaManagementContextConfiguration.class)
public class CustomMvcEndpoint {
    @Bean
    @ConfigurationProperties(prefix = "endpoints.admin.beans")
    BeansService beansService() {
        return new BeansService();
    }



    @Bean
    // @ConditionalOnEnabledEndpoint("admin/beans")
    @ConfigurationProperties(prefix = "endpoints.admin.beans")
    public BeansMvcEndpoint beansMvcEndpoint() {
        BeansMvcEndpoint beansMvcEndpoint = new BeansMvcEndpoint();
        return beansMvcEndpoint;
    }
}
