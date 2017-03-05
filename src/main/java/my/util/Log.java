package my.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common log function.
 * @author hubert
 */
public class Log {
    /**
     * match string like "{0.name}".
     */
    private final Pattern paramPattern = Pattern.compile("\\{(([0-9]+).([\\w]+))\\}");
    /**
     * delegate logger.
     */
    private final Logger  logger;



    private Log(Logger logger) {
        this.logger = logger;
    }



    public static Log of(Class<?> clazz) {
        return new Log(LoggerFactory.getLogger(clazz));
    }
    public static Log of(String name) {
        return new Log(LoggerFactory.getLogger(name));
    }



    // ====================log method ==============

    public void error(String pMsg, Object... pArgs) {
        if (isErrorEnabled()) {
            // TODO get invoke method name.
            String msg = messageFormat(pMsg, pArgs);
            logger.error(msg);
        }
    }



    public void error(Throwable pThrowable, String pMsg, Object... pArgs) {
        if (isErrorEnabled()) {
            logger.error(messageFormat(pMsg, pArgs), pThrowable);
        }
    }



    public void warn(Throwable pThrowable, String pMsg, Object... pArgs) {
        if (isWarnEnabled()) {
            logger.warn(messageFormat(pMsg, pArgs), pThrowable);
        }
    }



    public void warn(String pMsg, Object... pArgs) {
        if (isWarnEnabled()) {
            logger.warn(messageFormat(pMsg, pArgs));
        }
    }



    public void info(String pMsg, Object... pArgs) {
        if (isInfoEnabled()) {
            logger.info(messageFormat(pMsg, pArgs));
        }
    }



    public void debug(String pMsg, Object... pArgs) {
        if (isDebugEnabled()) {
            logger.debug(messageFormat(pMsg, pArgs));
        }
    }



    public void trace(String pMsg, Object... pArgs) {
        if (isTraceEnabled()) {
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



    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }



    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }



    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }



    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }



    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

}
