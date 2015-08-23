package com.codeyn.resouce.bus.redis;

public class RedisConfig {

    private final String hostName;
    private final int port;
    private int dbIndex = 0;
    private String password;

    public RedisConfig(String hostName) {
        this(hostName, 6379);
    }

    public RedisConfig(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

}
