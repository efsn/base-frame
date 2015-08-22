package com.codeyn.base.result;

import com.codeyn.base.exception.BusinessException;
import com.codeyn.base.exception.BusinessStatus;

public class ResultHelper {

	public static <T extends BaseResult> T failResult(T res,
			BusinessStatus status) {
		res.setSuccess(false);
		res.setCode(status.getCode());
		res.setMsg(status.getMessage());
		return res;
	}

	public static <T extends BaseResult> T failResult(T res, String message) {
		res.setSuccess(false);
		res.setCode(BusinessStatus.DEFAULT_ERROR_CODE);
		res.setMsg(message);
		return res;
	}

	public static BaseResult failResult(BusinessStatus status) {
		BaseResult res = new BaseResult();
		res.setSuccess(false);
		res.setCode(status.getCode());
		res.setMsg(status.getMessage());
		return res;
	}

	public static BaseResult failResult(String message) {
		BaseResult res = new BaseResult();
		res.setSuccess(false);
		res.setCode(BusinessStatus.DEFAULT_ERROR_CODE);
		res.setMsg(message);
		return res;
	}

	public static BaseResult sucResult() {
		BaseResult res = new BaseResult();
		res.setSuccess(true);
		return res;
	}

	public static BaseResult sucResult(String message) {
		BaseResult res = new BaseResult();
		res.setSuccess(true);
		res.setMsg(message);
		return res;
	}

	public static BaseResult sucResult(BusinessStatus status) {
		BaseResult res = new BaseResult();
		res.setSuccess(true);
		res.setCode(status.getCode());
		res.setMsg(status.getMessage());
		return res;
	}

	public static <T extends BaseResult> T sucResult(T res, String message) {
		res.setSuccess(true);
		res.setCode(BusinessStatus.DEFAULT_SUC_CODE);
		res.setMsg(message);
		return res;
	}

	public static <T extends BaseResult> T sucResult(T res,
			BusinessStatus status) {
		res.setSuccess(true);
		res.setCode(status.getCode());
		res.setMsg(status.getMessage());
		return res;
	}

	public static <T extends BaseResult> T copyBase(BaseResult source, T target) {
		target.setSuccess(false);
		target.setCode(source.getCode());
		target.setMsg(source.getMsg());
		return target;
	}

	public static void assertSucResult(BaseResult result)
			throws BusinessException {
		if (result != null && !result.isSuccess()) {
			throw new BusinessException(result.getCode(), result.getMsg());
		}
	}

	public static void assertSucResult(BaseResult result, BusinessStatus status)
			throws BusinessException {
		if (result != null && !result.isSuccess()) {
			throw new BusinessException(status);
		}
	}

	public static void assertSucResult(BaseResult result, String msg)
			throws BusinessException {
		if (result != null && !result.isSuccess()) {
			throw new BusinessException(msg);
		}
	}
}
