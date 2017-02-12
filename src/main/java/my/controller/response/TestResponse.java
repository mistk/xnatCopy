package my.controller.response;

import my.controller.response.dto.TestDTO;

public class TestResponse extends BaseRestResponse<TestDTO> {

    private static final long serialVersionUID = 1L;
    private int testInt = 11;
    private String str1;
    
    private String str2;

    public void setStr_1(String str) {
        this.str1 = str;
    }
    public String getStr1() {
        return str1;
    }
    
    public void setStr2(String pStr2) {
        str2 = pStr2;
    }
}
