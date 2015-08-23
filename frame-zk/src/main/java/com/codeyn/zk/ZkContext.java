package com.codeyn.zk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.codeyn.zk.core.ZkEngine;
import com.codeyn.zk.exception.ZookeeperException;

public class SqqZkContext {

    private static String HOSTS_PROPERTY_KEY = "zkHosts";
    private static String CLASSPATH_CONFIG_FILE = "resource-test.properties";

    private SqqZkContext() {
    }

    public static boolean isHostsSet() {
        return ZkEngineHolder.HOSTS != null;
    }

    public static ZkEngine getZkEngine() {
        if (ZkEngineHolder.zkEngine == null) {
            throw new ZookeeperException(ZkEngineHolder.errorMsg);
        }
        return ZkEngineHolder.zkEngine;
    }

    public static ZkEngine tryGetZkEngine() {
        return ZkEngineHolder.zkEngine;
    }

    private static class ZkEngineHolder {
        private static ZkEngine zkEngine;
        private static String HOSTS;
        private static String errorMsg = "";
        static {
            HOSTS = System.getProperty(HOSTS_PROPERTY_KEY);
            if (HOSTS == null) {
                try (InputStream in = ZkEngineHolder.class.getClassLoader().getResourceAsStream(CLASSPATH_CONFIG_FILE)) {
                    if (in != null) {
                        Properties prop = new Properties();
                        prop.load(in);
                        HOSTS = prop.getProperty(HOSTS_PROPERTY_KEY);
                    }
                } catch (IOException e) {
                }
            }
            if (HOSTS == null) {
                errorMsg = String.format(
                        "Init zkEngine failed. '%s' is not set. You may add '-D%s' to JVM's arguments,"
                                + " or put a config file under classpath with name '%s' witch has the property '%s'",
                        HOSTS_PROPERTY_KEY, HOSTS_PROPERTY_KEY, CLASSPATH_CONFIG_FILE, HOSTS_PROPERTY_KEY);
            } else {
                try {
                    zkEngine = new ZkEngine(HOSTS);
                } catch (Exception e) {
                    errorMsg = "Init zkEngine failed." + e.getMessage();
                }
                if (zkEngine != null) {
                    // 退出进程时自动清理
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            zkEngine.close();
                        }
                    });
                }
            }
        }
    }

}
