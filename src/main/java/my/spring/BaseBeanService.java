package my.spring;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import my.util.Log;

/**
 * provide base service for bean.
 * @author hubert
 */
public class BaseBeanService extends BaseBean {
    private final String  LOGGER_NAME = getClass().getName();
    protected final Log   log         = Log.of(LOGGER_NAME);
    private final Object  runningLock = new Object();
    private boolean       running;
    @Autowired
    private LoggingSystem loggingSystem;
    /**
     * service information.
     */
    private String        serviceInfo;



    public void startService() {
        log.info("start service bean: {0.beanName}", this);
        if (running) {
            log.warn("service already running!!");
            return;
        }
        synchronized (this.runningLock) {
            if (running) {
                return;
            }
            doStartService();
            // TODO proxy object.
            this.running = true;
        }
    }



    @PreDestroy
    public void stopService() {
        log.info("stop service bean: {0.beanName}", this);
        if (!running) {
            log.warn("service already stopping!!");
            return;
        }
        synchronized (this.runningLock) {
            if (!running) {
                return;
            }
            this.running = false;
            doStopService();
        }
    }



    // @EventListener
    // @Async

    protected void doStartService() {
        // TODO
    }



    protected void doStopService() {
        // TODO
    }



    /**
     * stand for this service start success.
     * @return
     */
    public boolean isRunning() {
        return this.running;
    }



    /**
     * @return the loggingInfo
     */
    public boolean isLoggingInfo() {
        return log.isInfoEnabled();
    }



    /**
     * @param pLoggingInfo
     *            the loggingInfo to set
     */
    public void setLoggingInfo(boolean pLoggingInfo) {
        if (pLoggingInfo) {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.INFO);
        } else {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.DEBUG);
        }
    }



    /**
     * @return the loggingWarning
     */
    public boolean isLoggingWarning() {
        return log.isWarnEnabled();
    }



    /**
     * @param pLoggingWarning
     *            the loggingWarning to set
     */
    public void setLoggingWarning(boolean pLoggingWarning) {
        if (pLoggingWarning) {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.WARN);
        } else {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.ERROR);
        }
    }



    /**
     * @return the loggingError
     */
    public boolean isLoggingError() {
        return log.isErrorEnabled();
    }



    /**
     * @param pLoggingError
     *            the loggingError to set
     */
    public void setLoggingError(boolean pLoggingError) {
        if (pLoggingError) {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.ERROR);
        } else {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.OFF);
        }
    }



    /**
     * @return the loggingDebug
     */
    public boolean isLoggingDebug() {
        return log.isDebugEnabled();
    }



    /**
     * @param pLoggingDebug
     *            the loggingDebug to set
     */
    public void setLoggingDebug(boolean pLoggingDebug) {
        if (pLoggingDebug) {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.DEBUG);
        } else {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.INFO);
        }
    }



    /**
     * @return the loggingTrace
     */
    public boolean isLoggingTrace() {
        return log.isTraceEnabled();
    }



    /**
     * @param pLoggingTrace
     *            the loggingTrace to set
     */
    public void setLoggingTrace(boolean pLoggingTrace) {
        if (pLoggingTrace) {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.TRACE);
        } else {
            loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.OFF);
        }
    }



    /**
     * @return the serviceInfo
     */
    public String getServiceInfo() {
        return serviceInfo;
    }



    /**
     * @param pServiceInfo
     *            the serviceInfo to set
     */
    public void setServiceInfo(String pServiceInfo) {
        serviceInfo = pServiceInfo;
    }

}