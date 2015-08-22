package com.codeyn.base.exception;

import java.util.HashMap;
import java.util.Map;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 7026776142859331595L;

	private BusinessStatus businessStatus;

	// 返回值对象
	private Map<String, Object> extendParams = new HashMap<String, Object>();

	public BusinessException(BusinessStatus status) {
		super();
		this.businessStatus = status;
	}

	public BusinessException(BusinessStatus status, Throwable cause) {
		super(cause);
		this.businessStatus = status;
	}

	public BusinessException(String message) {
		super();
		this.businessStatus = new DefaultStatus(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(cause);
		this.businessStatus = new DefaultStatus(message);
	}

	public BusinessException(int code, String message) {
		super();
		this.businessStatus = new DefaultStatus(code, message);
	}

	public BusinessException(int code, String message, Throwable cause) {
		super(cause);
		this.businessStatus = new DefaultStatus(code, message);
	}

	public BusinessException addParam(String name, Object param) {
		this.extendParams.put(name, param);
		return this;
	}

	public Object getParam(String name) {
		return this.extendParams.get(name);
	}

	public String getString(String name) {
		Object obj = this.extendParams.get(name);
		return obj == null ? null : obj.toString();
	}

	public Integer getInt(String name) {
		Object obj = this.extendParams.get(name);
		return obj == null ? null : Integer.valueOf(obj.toString());
	}

	public Map<String, Object> getParamMap() {
		return this.extendParams;
	}

	public BusinessStatus getBusinessStatus() {
		return businessStatus;
	}

	public int getExceptionCode() {
		return businessStatus.getCode();
	}

	public String getExceptionMessage() {
		return businessStatus.getMessage();
	}

	@Override
	public String getMessage() {
		String statusLine = String.format(" %s | %s ",
				businessStatus.getCode(), businessStatus.getMessage());
		StringBuilder message = new StringBuilder(statusLine);
		if (!extendParams.isEmpty()) {
			message.append('[');
			boolean start = true;
			for (String key : extendParams.keySet()) {
				if (start) {
					start = false;
				} else {
					message.append(" | ");
				}
				message.append(key).append("=").append(extendParams.get(key));
			}
			message.append(']');
		}
		return message.toString();
	}
}
