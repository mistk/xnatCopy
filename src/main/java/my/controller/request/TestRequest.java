package my.controller.request;

public class TestRequest extends BaseRestRequest {
    private static final long serialVersionUID = 1L;
    private int param1;
    private String param2;
    
    public void setParam1(int pParam1) {
        param1 = pParam1;
    }
    public void setParam2(String pParam2) {
        param2 = pParam2;
    }
    public int getParam1() {
        return param1;
    }
    public String getParam2() {
        return param2;
    }
}
