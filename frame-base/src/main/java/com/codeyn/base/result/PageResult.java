package com.codeyn.base.result;

public class PageResult<T> extends ListResult<T> {

    private static final long serialVersionUID = 1L;

    private int total;
    private int pageNo;
    private int pageSize;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("PageResult [isSuccess=").append(isSuccess).append(", code=").append(code).append(", msg=")
                .append(msg).append(", dataList=")
                .append(dataList != null ? dataList.subList(0, Math.min(dataList.size(), maxLen)) : null)
                .append(", total=").append(total).append(", pageNo=").append(pageNo).append(", pageSize=")
                .append(pageSize).append("]");
        return builder.toString();
    }

}
