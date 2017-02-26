package my.test;

import java.util.regex.Pattern;

public class Test {
    int arr[]=new int[10]; 
	public static void main(String[] args) {
	    String s = "s";
	    System.out.println(String.class.isAssignableFrom(s.getClass()));
	}
	static int test () {
	    int i = 2;
	    try {
            return i;
        } finally {
            ++i;
            System.out.println("sssssssss");
        }
	}
}
