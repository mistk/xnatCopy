package my.test;

import org.apache.mina.filter.logging.LogLevel;

public class Test {
	public static void main(String[] args) {
//		System.out.println(LogLevel.valueOf("trace"));
		System.out.println(org.springframework.boot.logging.LogLevel.valueOf("trace"));
	}
}
