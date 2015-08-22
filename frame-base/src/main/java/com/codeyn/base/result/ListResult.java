package com.codeyn.base.result;

import java.util.ArrayList;
import java.util.List;

public class ListResult<T> extends BaseResult {

    private static final long serialVersionUID = 1L;

    protected final List<T> dataList = new ArrayList<>();

    public List<T> getDataList() {
        return dataList;
    }

    public ListResult<T> add(T data) {
        this.dataList.add(data);
        return this;
    }

    public ListResult<T> addAll(List<T> dataList) {
        this.dataList.addAll(dataList);
        return this;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("ListResult [isSuccess=").append(isSuccess).append(", code=").append(code).append(", msg=")
                .append(msg).append(", dataList=")
                .append(dataList != null ? dataList.subList(0, Math.min(dataList.size(), maxLen)) : null).append("]");
        return builder.toString();
    }

}
