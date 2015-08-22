package com.codeyn.base.upyun;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpYunSpace {
    /**
     * 空间名称
     */
    private String space;

    /**
     * 回调URL
     */
    private String notifyUrl;

    /**
     * 表单MD5签名密钥
     */
    private String sercetKey;

    /**
     * 有效期，单位秒
     */
    private int validitySeconds = 120;

    public UpYunFormParams createFormParams() {
        return createFormParams(null);
    }

    public UpYunFormParams createFormParams(Map<String, Object> upParams) {
        if (upParams == null) {
            upParams = new HashMap<>();
        }
        long expiration = System.currentTimeMillis() / 1000 + validitySeconds;
        try {
            upParams.put("notify-url", notifyUrl);
            String policy = UpYunUtils.makePolicy(getSaveKey(), expiration, space, upParams);
            String sign = UpYunUtils.signature(policy + "&" + sercetKey);
            UpYunFormParams params = new UpYunFormParams();
            params.setPolicy(policy);
            params.setSignature(sign);
            return params;
        } catch (UpYunException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getSaveKey() {
        String fileName = UUID.randomUUID().toString().replace("-", "");
        return "/{year}/{mon}/{day}/" + fileName + "{sec}{.suffix}";
    }

    public void setValiditySeconds(int validitySeconds) {
        this.validitySeconds = validitySeconds;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public void setSercetKey(String sercetKey) {
        this.sercetKey = sercetKey;
    }

}
