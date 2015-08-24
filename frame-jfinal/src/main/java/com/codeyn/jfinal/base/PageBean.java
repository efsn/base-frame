package com.codeyn.jfinal.base;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Codeyn
 *
 */
public class PageBean<T> {

    public final static int MAX_PAGE_SIZE = -1;

    private int pageNo;

    private int totalRow;

    private int pageSize;

    private Map<String, String> ext = new HashMap<>();

    private String prop;

    private String order;

    private List<T> list;

    private String url;

    private int startNo;

    private int endNo;

    public Map<String, String> getExt() {
        return ext;
    }

    public void set(Map<String, String> ext) {
        this.ext = ext;
    }

    public int getMaxPageNo() {
        int size = getPageSize();

        if (size == -1) return 1;
        if (totalRow == 0) return 1;

        return (totalRow / size) + (totalRow % size == 0 ? 0 : 1);
    }

    public int getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(int totalRow) {
        this.totalRow = totalRow;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }

    public int getPageSize() {
        if (pageSize == -1) return -1;
        return pageSize <= 0 ? 15 : pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo <= 0 ? 1 : pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public List<T> getList() {
        return list == null ? new ArrayList<T>() : list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isNext() {
        return getMaxPageNo() > getPageNo();
    }

    public boolean isPre() {
        return getPageNo() > 1;
    }

    public String getTogger() {
        return "asc".equalsIgnoreCase(order) ? "desc" : "asc";
    }

    public void setDefaultOrder(String prop, String order) {
        this.prop = prop;
        this.order = order;
    }

    public int getOffset() {
        return (getPageNo() - 1) * getPageSize();
    }

    public String getUrl() {
        if (url == null) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<Map.Entry<String, String>> itr = ext.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<String, String> entry = itr.next();
                sb.append("&").append(entry.getKey()).append("=");
                if (entry.getValue() != null) {
                    try {
                        sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                sb.append("&").append("pageSize=").append(getPageSize());
                sb.deleteCharAt(0);
            }
            return sb.toString();
        }
        return null;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullUrl() {
        StringBuffer sb = new StringBuffer(getUrl());
        if (StringUtils.isNotBlank(prop)) {
            sb.append("&").append("prop=").append(getProp());
        }
        if (StringUtils.isNotBlank(order)) {
            sb.append("&").append("order=").append(getOrder());
        }
        return sb.toString();
    }

    public int getStartNo() {
        if (startNo == 0) initNo();
        return startNo;
    }

    public void setStartNo(int startNo) {
        this.startNo = startNo;
    }

    public int getEndNo() {
        if (endNo == 0) initNo();
        return endNo;
    }

    public void setEndNo(int endNo) {
        this.endNo = endNo;
    }

    private void initNo() {
        int maxPageNo = getMaxPageNo();
        if (maxPageNo > 7) {
            startNo = this.pageNo - 2;
            if (startNo < 2) {
                startNo = 2;
            }
            endNo = this.pageNo + 2;
            if (endNo > maxPageNo - 1) {
                endNo = maxPageNo - 1;
            }
            if ((endNo - startNo) < 4) {
                if ((endNo - 4) > 1) {
                    startNo = endNo - 4;
                } else {
                    startNo = 2;
                }
                if ((startNo + 4) < (this.getMaxPageNo() - 1)) {
                    endNo = startNo + 4;
                } else {
                    endNo = this.getMaxPageNo() - 1;
                }
            }
        }
    }

}
