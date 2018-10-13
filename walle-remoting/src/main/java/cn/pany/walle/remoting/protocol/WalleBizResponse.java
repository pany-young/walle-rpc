package cn.pany.walle.remoting.protocol;

import java.util.Date;

/**
 * Created by pany on 16/9/4.
 */
public class WalleBizResponse {

    private String requestId;
    private Throwable error;
    private Object result;

    private Boolean success;
    private Date receiveTime;
    private Long timeOutNum;


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

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Long getTimeOutNum() {
        return timeOutNum;
    }

    public void setTimeOutNum(Long timeOutNum) {
        this.timeOutNum = timeOutNum;
    }
}
