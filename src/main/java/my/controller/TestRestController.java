package my.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import my.constanst.CommonConstants;
import my.controller.request.TestRequest;
import my.controller.response.TestResponse;

/**
 * controller invoke DTOGenerator and service.
 * @author xnat
 */
@RestController
@RequestMapping("rest")
public class TestRestController extends BaseController {

    @RequestMapping("json")
    TestResponse json(@RequestBody(required = false) TestRequest pRequest) {
        log.info("request: {0}", pRequest);
        TestResponse resp = new TestResponse();
        resp.setSuccess(true);
        resp.setState(null);
        resp.setStr2("str2");
        resp.setStr_1("str_1");
        log.debug("response: {0.class}, abcdac {0.str1}", resp);
        return resp;
    }



    @GetMapping("test")
    String test() {
        return "hello world";
    }



    // should be use actuator
    @RequestMapping("h2info")
    String getH2Info() {
        return CommonConstants.EMPTY;
    }
}
