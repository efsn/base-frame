package com.codeyn.jfinal.base;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeyn.base.exception.BusinessStatus;
import com.codeyn.base.result.DataResult;
import com.codeyn.base.result.PageResult;
import com.codeyn.base.result.ResultHelper;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.render.FreeMarkerRender;

import freemarker.template.Template;

/**
 * 
 * @author Codeyn
 *
 */
public class BaseController extends Controller {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public void afterInterceptor() {

    }

    public <T> PageResult<T> getPageResult(Page<T> page) {
        PageResult<T> pr = new PageResult<>();
        pr.setPageNo(page.getPageNumber());
        pr.setPageSize(page.getPageSize());
        pr.setTotal(page.getTotalRow());
        pr.addAll(page.getList());
        return pr;
    }

    public <T> PageBean<T> getPageBean(Page<T> page) {
        PageBean<T> pb = new PageBean<>();
        pb.setPageNo(page.getPageNumber());
        pb.setPageSize(page.getPageSize());
        pb.setTotalRow(page.getTotalRow());
        pb.setList(page.getList());
        return pb;
    }

    public void renderSucResult() {
        renderJson(new DataResult());
    }

    public void renderSucResult(Map<String, Object> data) {
        DataResult dr = new DataResult().putAll(data);
        renderJson(dr);
    }

    public void renderSucResult(Map<String, Object> data, String msg) {
        DataResult dr = new DataResult().putAll(data);
        dr.setMsg(msg);
        renderJson(dr);
    }

    public void renderFailResult(String msg) {
        renderJson(ResultHelper.failResult(msg));
    }

    public void renderFailResult(BusinessStatus status) {
        renderJson(ResultHelper.failResult(status));
    }
    
    /**
     * 解析子页面
     */
    @SuppressWarnings("unchecked")
    protected String parseTemplte(String view) {
        StringWriter sw = new StringWriter();
        try {
            Map<String, Object> map = new HashMap<>();
            for (Enumeration<String> attr = getRequest().getAttributeNames(); attr.hasMoreElements();) {
                String name = attr.nextElement();
                map.put(name, getRequest().getAttribute(name));
            }
            Template tmp = FreeMarkerRender.getConfiguration().getTemplate("/WEB-INF/pages" + view);
            tmp.process(map, sw);
        } catch (Exception e) {
            logger.error("parse sub template error. view: " + view, e);;
        }
        return sw.toString();
    }

}
