package my.spring;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

/**
 * provide base service for bean.
 * @author hubert
 */
public class BaseBeanService extends BaseBean {
    private final String  LOGGER_NAME  = getClass().getName();
    private final Logger  logger       = LoggerFactory.getLogger(LOGGER_NAME);
    private final Object  runningLock  = new Object();
    /**
     * match string like "{0.name}".
     */
    private final Pattern paramPattern = Pattern.compile("\\{(([0-9]+).([\\w]+))\\}");
    private boolean       running;
    @Autowired
    private LoggingSystem loggingSystem;
    /**
     * service information.
     */
    private String        serviceInfo;



    public void startService() {
        logInfo("start service bean: {0.beanName}", this);
        if (running) {
            logWarn("service already running!!");
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
        logInfo("stop service bean: {0.beanName}", this);
        if (!running) {
            logWarn("service already stopping!!");
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



    // ====================log method ==============

    public void logError(String pMsg, Object... pArgs) {
        if (isLoggingError()) {
            // TODO get invoke method name.
            String msg = messageFormat(pMsg, pArgs);
            logger.error(msg);
        }
    }



    public void logError(Throwable pThrowable, String pMsg, Object... pArgs) {
        if (isLoggingError()) {
            logger.error(messageFormat(pMsg, pArgs), pThrowable);
        }
    }



    public void logWarn(Throwable pThrowable, String pMsg, Object... pArgs) {
        if (isLoggingWarning()) {
            logger.warn(messageFormat(pMsg, pArgs), pThrowable);
        }
    }



    public void logWarn(String pMsg, Object... pArgs) {
        if (isLoggingWarning()) {
            logger.warn(messageFormat(pMsg, pArgs));
        }
    }



    public void logInfo(String pMsg, Object... pArgs) {
        if (isLoggingInfo()) {
            logger.info(messageFormat(pMsg, pArgs));
        }
    }



    public void logDebug(String pMsg, Object... pArgs) {
        if (isLoggingDebug()) {
            logger.debug(messageFormat(pMsg, pArgs));
        }
    }



    public void logTrace(String pMsg, Object... pArgs) {
        if (isLoggingTrace()) {
            logger.trace(messageFormat(pMsg, pArgs));
        }
    }



    // ====================log method ==============

    /**
     * ("abcd {0.name} {1.prop} and {0.age}", person, obj) ("abcd {0.name}
     * {1.prop} and {0.age}", name, obj, age)
     * @param pFormat
     *            need format msg string.
     * @param pArgs
     *            arguments.
     * @return formated result msg.
     */
    private String messageFormat(String pFormat, Object... pArgs) {
        if (ArrayUtils.isEmpty(pArgs)) {
            return pFormat;
        }
        Matcher matcher = paramPattern.matcher(pFormat);
        // search string like "{0.name}" in pFormat
        if (!matcher.find()) {
            return MessageFormat.format(pFormat, pArgs);
        }
        StrBuilder sb = new StrBuilder(pFormat);
        matcher = paramPattern.matcher(sb);
        List<Object> args = new ArrayList<>(pArgs.length + 2);
        // "{0.name}" regex resolve:
        // group(1) => {0.name}
        // group(2) => 0
        // group(3) => name
        int count = 0;
        while (matcher.find()) {
            Object indexMappedObject = null;
            try {
                int index = NumberUtils.toInt(matcher.group(2), -1);
                if (index < 0) {
                    continue;
                }
                String propExpression = matcher.group(3);
                indexMappedObject = BeanUtils.getProperty(pArgs[index], propExpression);
            } catch (Exception e) {
                // ignore. set it's mapped value is null.
            }
            args.add(indexMappedObject);
            // change "{0.name}" to "{0}".
            sb = sb.replaceFirst(matcher.group(1), String.valueOf(count));
            matcher = paramPattern.matcher(sb);
            count++;
        }
        return MessageFormat.format(sb.toString(), args.toArray());
    }



    /**
     * @return the loggingInfo
     */
    public boolean isLoggingInfo() {
        return logger.isInfoEnabled();
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
        return logger.isWarnEnabled();
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
        return logger.isErrorEnabled();
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
        return logger.isDebugEnabled();
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
        return logger.isTraceEnabled();
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