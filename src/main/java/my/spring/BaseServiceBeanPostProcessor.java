package my.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class BaseServiceBeanPostProcessor implements BeanPostProcessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());



    @Override
    public Object postProcessBeforeInitialization(Object pBean, String pBeanName) throws BeansException {
        return pBean;
    }



    @Override
    public Object postProcessAfterInitialization(Object pBean, String pBeanName) throws BeansException {
        if (pBean instanceof BaseBeanService) {
            BaseBeanService serviceBean = ((BaseBeanService) pBean);
            try {
                serviceBean.startService();
            } catch (Exception e) {
                logger.error("start service bean error.", e);
            }
        }
        return pBean;
    }

}
