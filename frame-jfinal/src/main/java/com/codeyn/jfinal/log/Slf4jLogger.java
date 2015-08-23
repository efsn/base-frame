package com.codeyn.jfinal.log;

import org.slf4j.LoggerFactory;

import com.jfinal.log.Logger;

public class Slf4jLogger extends Logger {

    private org.slf4j.Logger proxy;

    public Slf4jLogger(String name) {
        proxy = LoggerFactory.getLogger(name);
    }

    public Slf4jLogger(Class<?> clazz) {
        proxy = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(String message) {
        proxy.debug(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        proxy.debug(message, t);
    }

    @Override
    public void info(String message) {
        proxy.info(message);
    }

    @Override
    public void info(String message, Throwable t) {
        proxy.info(message, t);
    }

    @Override
    public void warn(String message) {
        proxy.warn(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        proxy.warn(message, t);
    }

    @Override
    public void error(String message) {
        proxy.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        proxy.error(message, t);
    }

    @Override
    public void fatal(String message) {
        proxy.error(message);
    }

    @Override
    public void fatal(String message, Throwable t) {
        proxy.error(message, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return proxy.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return proxy.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return proxy.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return proxy.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return false;
    }

}
