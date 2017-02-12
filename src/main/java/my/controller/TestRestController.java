package my.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import my.constanst.CommonConstants;
import my.controller.request.TestRequest;
import my.controller.response.TestResponse;

/**
 * controller invoke DTOGenerator and service.
 * @author xnat
 *
 */
@RestController
@RequestMapping("rest")
public class TestRestController {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
	@RequestMapping("json")
	TestResponse json(@RequestBody TestRequest pRequest) {
	    logger.debug("request: {}", pRequest);
	    TestResponse resp = new TestResponse();
	    resp.setSuccess(true);
	    resp.setState(null);
	    resp.setStr2("str2");
	    resp.setStr_1("str_1");
	    logger.debug("response: {}", resp);
	    return resp;
	}
	
	
	
	@RequestMapping("test")
	String test() {
		return "hello world";
	}
	@RequestMapping("h2info")
	String getH2Info() {
		return CommonConstants.EMPTY;
	}
}
