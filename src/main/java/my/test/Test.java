package my.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args) {
		String configStr = "[true]{String}";
		configStr = "func(a,b)";
		Pattern p = Pattern.compile("^\\[(.+)\\](\\{[a-zA-Z]+\\})?$");
//		p = Pattern.compile("\\[(.+)\\]");
//		p = Pattern.compile("\\{(.+)\\}");
		p = Pattern.compile("^([a-zA-Z]+)(\\([a-zA-Z0-9]+(,[a-zA-Z0-9])*\\))?$");
		Matcher matcher = p.matcher(configStr);
		if (matcher.find()) {
			System.out.println(matcher.group());
			System.out.println(matcher.group(1));
			System.out.println(matcher.group(2));
		}
	}
}
