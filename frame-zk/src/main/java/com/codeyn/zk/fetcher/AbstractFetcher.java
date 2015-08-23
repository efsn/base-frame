package com.codeyn.zk.fetcher;

import com.codeyn.zk.core.ZkNodeStore;

public abstract class AbstractFetcher {

    protected ZkNodeStore zkNodeStore;

    private String znodePath;

    private volatile boolean isClosed = false;

    protected Object convertedObj;

    public AbstractFetcher(ZkNodeStore zkNodeStore) {
        this.zkNodeStore = zkNodeStore;
        this.znodePath = zkNodeStore.getZnodePath();
    }

    public String getZnodePath() {
        return znodePath;
    }

    public synchronized final void close() {
        if (isClosed) {
            return;
        }
        onClose();
        isClosed = true;
        zkNodeStore = null;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public Object getConvertedObject() {
        return convertedObj;
    }

    protected abstract void onClose();

}
