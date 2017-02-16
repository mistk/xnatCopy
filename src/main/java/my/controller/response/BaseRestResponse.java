package my.controller.response;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * base rest response structure object.
 * 
 * @author hubert
 */
public class BaseRestResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private T state;


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    /**
     * result status.
     * @return true if success.
     */
    public boolean isSuccess() {
        return success;
    }


    /**
     * result data, general is a dto.
     * @return
     */
    public T getState() {
        return state;
    }


    public void setSuccess(boolean pSuccess) {
        success = pSuccess;
    }


    public void setState(T pState) {
        state = pState;
    }



}
