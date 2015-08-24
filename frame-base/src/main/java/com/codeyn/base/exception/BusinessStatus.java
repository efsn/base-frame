package com.codeyn.base.exception;

public interface BusinessStatus {

    public final static int DEFAULT_ERROR_CODE = 500;

    public final static int DEFAULT_SUC_CODE = 200;

    public int getCode();

    public String getMessage();
}
