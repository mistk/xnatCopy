package my.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rest")
public class TestRestController {
	@RequestMapping("test")
	String test() {
		return "hello world";
	}
}
