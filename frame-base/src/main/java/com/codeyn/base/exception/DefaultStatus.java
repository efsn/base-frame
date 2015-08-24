package com.codeyn.base.exception;

public class DefaultStatus implements BusinessStatus {

    private int code;
    private String message;

    public DefaultStatus(String message) {
        this.code = DEFAULT_ERROR_CODE;
        this.message = message;
    }

    public DefaultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
