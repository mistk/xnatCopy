package my.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.StrBuilder;

public class Test {
    int arr[]=new int[10]; 
    static Pattern paramPattern = Pattern.compile("\\{(([0-9]+).([\\w]+))\\}");
	public static void main(String[] args) {
	    String msg = "response: {0.class}, {0.class} abcdac {1.str1}";
//	    StrBuilder sb = new StrBuilder(msg);
//	    sb.replaceFirst("0.class", "0");
//	    sb.replaceFirst("0.class", "0");
//	    System.out.println(sb.toString());
	    messageFormat(msg, new Object());
	}
	static String messageFormat(String pFormat, Object... pArgs) {
        if (ArrayUtils.isEmpty(pArgs)) {
            return pFormat;
        }
        Matcher matcher = paramPattern.matcher(pFormat);
        if (!matcher.find()) {
            return MessageFormat.format(pFormat, pArgs);
        }
        StrBuilder sb = new StrBuilder(pFormat);
        matcher = paramPattern.matcher(sb);
        List<Object> args = new ArrayList<>(pArgs.length + 2);
        // exist many same index e.g: ("abcd {0.name} {1.prop} and {0.age}", person, obj)
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
            // change 0.name to 0.
            sb = sb.replaceFirst(matcher.group(1), String.valueOf(count));
//            matcher = paramPattern.matcher(sb);
            count++;
        }
        return MessageFormat.format(sb.toString(), args);
    }

}
