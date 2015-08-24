package com.codeyn.base.result;

import java.io.Serializable;

import com.codeyn.base.exception.BusinessStatus;

public class BaseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    protected boolean isSuccess = true;

    protected int code = BusinessStatus.DEFAULT_SUC_CODE; // 响应状态码 默认200代表成功

    protected String msg = "SUCCESS";; // 状态描述信息

    public BaseResult() {
    }

    public BaseResult(boolean isSuccess, int code, String msg) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String toString() {
        StringBuffer builder = new StringBuffer();
        builder.append("BaseResult [isSuccess=").append(isSuccess).append(", code=").append(code).append(", msg=")
                .append(msg).append("]");
        return builder.toString();
    }

}
