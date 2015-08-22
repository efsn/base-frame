package com.codeyn.base.upyun;

import com.alibaba.fastjson.JSON;

public class UpYunFormParams {

    private String space;

    private String policy;

    private String signature;

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

    @Override
    public String toString() {
        return String.format("UpYunFormParams [space=%s, policy=%s, signature=%s]", space, policy, signature);
    }

}
