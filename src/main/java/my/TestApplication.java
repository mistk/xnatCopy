package my;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {
	public static void main(String[] args) {
//		String jvmArgs = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8787";
		SpringApplication.run(TestApplication.class, args);
	}
}
