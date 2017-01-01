package my.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import my.CommonConstants;

@RestController
@RequestMapping("rest")
public class TestRestController {
	@RequestMapping("test")
	String test() {
		return "hello world";
	}
	@RequestMapping("h2info")
	String getH2Info() {
		return CommonConstants.EMPTY;
	}
}
