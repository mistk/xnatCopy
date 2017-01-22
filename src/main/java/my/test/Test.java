package my.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args) {
	    String s = "obj[1]";
//	    s = "ss";
	    Pattern p = Pattern.compile("^([a-zA-Z0-9]+)(\\[([0-9])\\])?$");
	    Matcher m = p.matcher(s);
	    if (m.find()) {
	        System.out.println(m.group());
	        System.out.println(m.group(1));
	        System.out.println(m.group(2));
	        System.out.println(m.group(3));
	    }
	    String[] arr = {"aa", "bb"};
	    Object o = arr;
	    System.out.println(o.getClass().isArray());
	}
}
