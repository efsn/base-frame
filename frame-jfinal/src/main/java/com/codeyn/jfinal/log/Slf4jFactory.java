package com.codeyn.jfinal.log;

import com.jfinal.log.ILogFactory;
import com.jfinal.log.Log;
import com.jfinal.log.LogManager;

public class Slf4jFactory implements ILogFactory {

    private Slf4jFactory() {
    }

    public static void adaptToJFinal() {
        LogManager.me().setDefaultLogFactory(Singleton.me);
    }

    @Override
    public Log getLog(Class<?> clazz) {
        return new Slf4jLog(clazz);
    }

    @Override
    public Log getLog(String name) {
        return new Slf4jLog(name);
    }

    private static class Singleton {
        private static Slf4jFactory me = new Slf4jFactory();
    }

}
