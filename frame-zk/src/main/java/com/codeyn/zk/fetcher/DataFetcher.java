package com.codeyn.zk.fetcher;

import java.io.UnsupportedEncodingException;

import com.codeyn.zk.core.ZkNodeStore;

public class DataFetcher extends AbstractFetcher {

    private byte[] lastSnapShot;
    private DataConverter<?> converter;

    public DataFetcher(ZkNodeStore zkNodeStore) {
        super(zkNodeStore);
    }

    public DataFetcher(ZkNodeStore zkNodeStore, DataConverter<?> converter) {
        super(zkNodeStore);
        this.converter = converter;
    }

    public byte[] getDataBytes() {
        byte[] bytes = isClosed() ? lastSnapShot : zkNodeStore.getData();
        return bytes == null ? null : bytes;
    }

    public String getDataAsString(String charSet) {
        byte[] bytes = getDataBytes();
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, charSet);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDataAsString() {
        return getDataAsString("UTF-8");
    }

    public synchronized void onDataUpdate() {
        if (converter != null && !isClosed()) {
            byte[] data = zkNodeStore.getData();
            if (data == null) {
                convertedObj = null;
            } else {
                convertedObj = converter.convert(data);
            }
        }
    }

    @Override
    protected void onClose() {
        zkNodeStore.removeDataFetcher(this);
        lastSnapShot = zkNodeStore.getData();
    }
}
