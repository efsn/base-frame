package com.codeyn.jfinal.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeyn.base.exception.BusinessException;
import com.codeyn.base.exception.DefaultStatus;
import com.codeyn.base.result.ResultHelper;
import com.codeyn.utils.HttpUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.ActionException;
import com.jfinal.core.Controller;

public class ExceptionInterceptor implements Interceptor {

    private static final String ERROR_VIEW = "<html><head><title>Internal Server Error</title></head><body bgcolor='white'>%s</body></html>";
    private static final Logger logger = LoggerFactory.getLogger(ExceptionInterceptor.class);

    private Set<String> jsonExts = new HashSet<>();
    private boolean devMode;

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public void intercept(Invocation inv) {
        try {
            inv.invoke();
        } catch (RuntimeException e) {
            handleException(inv, e);
        }
    }

    public void addJsonExts(String... exts) {
        if (exts == null) return;
        for (int i = 0; i < exts.length; i++) {
            exts[i] = ".".concat(exts[i]);
        }
        jsonExts.addAll(Arrays.asList(exts));
    }

    private void handleException(Invocation inv, RuntimeException e) {
        Controller c = inv.getController();
        if (isJsonResult(c.getRequest())) {
            logError(inv, e);
            renderJsonError(c, e);
        } else {
            if (devMode) {
                logError(inv, e);
                renderDebugErrorView(c, e);
            } else {
                c.setAttr("exception", e);
                throw e;
            }
        }
    }

    private void logError(Invocation inv, RuntimeException e) {
        logger.error(
                String.format("action %s (%s.%s) exception:  %s", inv.getActionKey(), inv.getMethod()
                        .getDeclaringClass().getSimpleName(), inv.getMethodName(), e.getMessage()), e);
    }

    private void renderDebugErrorView(Controller c, RuntimeException e) {
        StringBuffer sb = new StringBuffer();
        sb.append("<span>Internal server error:</span><br/>");
        OutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));
        sb.append(StringUtils.replace(out.toString(), System.lineSeparator(), "<br/>"));
        c.renderHtml(String.format(ERROR_VIEW, sb.toString()));
        c.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void renderJsonError(Controller c, RuntimeException e) {
        while (e != null) {
            if (e instanceof BusinessException) {
                BusinessException be = (BusinessException) e;
                c.renderJson(ResultHelper.failResult(be.getBusinessStatus()));
                return;
            } else if (e instanceof ActionException) {
                ActionException ae = (ActionException) e;
                c.renderJson(ResultHelper.failResult(new DefaultStatus(ae.getErrorCode(), "Internal server error")));
                return;
            } else if (e.getClass().getName().equals("org.springframework.dao.DuplicateKeyException")) {
                c.renderJson(ResultHelper.failResult("Unique error"));
                return;
            }
            e = (RuntimeException) e.getCause();
        }
        c.renderJson(ResultHelper.failResult("Internal server error"));
    }

    private boolean isJsonResult(HttpServletRequest request) {
        if (HttpUtil.isAjax(request)) {
            return true;
        }
        String uri = request.getRequestURI();
        if (jsonExts != null) {
            for (String ext : jsonExts) {
                if (uri.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

}
