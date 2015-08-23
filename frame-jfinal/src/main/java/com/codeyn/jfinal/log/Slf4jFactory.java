package com.codeyn.jfinal.log;

import com.jfinal.log.ILoggerFactory;
import com.jfinal.log.Logger;

public class Slf4jFactory implements ILoggerFactory {

    private Slf4jFactory() {
    }

    public static void adaptToJFinal() {
        Logger.setLoggerFactory(Singleton.me);
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new Slf4jLogger(clazz);
    }

    @Override
    public Logger getLogger(String name) {
        return new Slf4jLogger(name);
    }

    private static class Singleton {
        private static Slf4jFactory me = new Slf4jFactory();
    }

}
