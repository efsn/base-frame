package com.codeyn.resouce.bus.mq.config;

public class MqSpaceConfig {

    private String host;
    private int port;
    private String addresses;
    private String vhost;
    private String username;
    private String password;

    public MqSpaceConfig() {
    }

    public MqSpaceConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    @Override
    public String toString() {
        return String.format(
                "ConnectionFactoryConfig [host=%s, port=%s, addresses=%s, vhost=%s, username=%s,use password=%s]",
                host, port, addresses, vhost, username, password != null);
    }

}
