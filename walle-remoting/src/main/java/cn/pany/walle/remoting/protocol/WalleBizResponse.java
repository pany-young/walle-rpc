package cn.pany.walle.remoting.protocol;

/**
 * Created by pany on 16/9/4.
 */
public class WalleBizResponse {

    private String requestId;
    private Throwable error;
    private Object result;


    private Boolean success;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

}
