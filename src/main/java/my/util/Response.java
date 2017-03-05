package my.util;

import java.util.HashMap;
import java.util.Map;

/**
 * used for common method return info.
 * @author hubert
 */
public class Response {
    private final Map<String, Object> result    = new HashMap<>(2);
    public static final String        SUCCESS   = "success";
    public static final String        ERROR_MSG = "errorMsg";



    public Response() {}
    /**
     * set default result success.
     * @param pSuccess
     */
    public Response(Boolean pSuccess) {
        setSuccess(pSuccess);
    }



    /**
     * setErrorMsg
     * @param pErrorMsg
     */
    public void setErrorMsg(String pErrorMsg) {
        result.put(ERROR_MSG, pErrorMsg);
    }



    /**
     * @return errorMsg
     */
    public String getErrorMsg() {
        return (String) result.get(ERROR_MSG);
    }



    /**
     * setSuccess
     * @param pSuccess
     */
    public void setSuccess(Boolean pSuccess) {
        result.put(SUCCESS, pSuccess);
    }



    /**
     * @return success.
     */
    public boolean isSuccess() {
        return (boolean) result.get(SUCCESS);
    }
}
