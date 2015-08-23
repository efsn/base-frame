package com.codeyn.zk.core;

public class ZkNode {

    private String path;
    private byte[] data;

    public ZkNode(String path) {
        this(path, null);
    }

    public ZkNode(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ZkNode [path=" + path + ",  data.length=" + data.length + "]";
    }

}
