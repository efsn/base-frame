package com.codeyn.base.log;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class WebLogFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger("WebLogger");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        MDC.put("token", makeToken());
        try {
            if (logger.isInfoEnabled()) {
                String path = "";
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                    path = httpServletRequest.getRequestURI();
                }
                logger.info("Access log: {} {} {}", request.getRemoteHost(), path, makeParamLog(request));
            }
            chain.doFilter(request, response);
        } catch (Throwable e) {
            Throwable ex = e;
            if (e instanceof ServletException && e.getCause() != null) {
                ex = e.getCause();
            }
            logger.error("error happend: " + ex.getMessage(), ex);
            throw e;
        } finally {
            MDC.remove("token");
        }
    }

    @SuppressWarnings("unchecked")
    private String makeParamLog(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues != null) {
                String valStr = "";
                if (paramValues.length == 1) {
                    valStr = paramValues[0];
                } else if (paramValues.length > 1) {
                    valStr = Arrays.toString(paramValues);
                }
                sb.append(paramName).append("=").append(valStr);
                if (parameterNames.hasMoreElements()) {
                    sb.append("|");
                }
            }
        }
        return sb.toString();
    }

    private String makeToken() {
        //生成请求token加到日志上下文中
        String nanoPart = String.format("%03d", System.nanoTime() % 1000);
        return String.valueOf(System.currentTimeMillis()).substring(8) + nanoPart;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
