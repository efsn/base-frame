package com.codeyn.zk.fetcher;

import java.util.List;

import com.codeyn.zk.core.ZkNodeStore;

public class ChildrenFetcher extends AbstractFetcher {

    private List<String> lastSnapShot;
    private ChildrenConverter<?> converter;

    public ChildrenFetcher(ZkNodeStore zkNodeStore) {
        super(zkNodeStore);
    }

    public ChildrenFetcher(ZkNodeStore zkNodeStore, ChildrenConverter<?> converter) {
        super(zkNodeStore);
        this.converter = converter;
    }

    public List<String> getNodeChildren() {
        List<String> children = isClosed() ? lastSnapShot : zkNodeStore.getChildren();
        return children == null ? null : children;
    }

    public boolean contains(String childName) {
        List<String> children = getNodeChildren();
        if (children == null) {
            return false;
        }
        return children.contains(childName);
    }

    public synchronized void onChildrenUpdate() {
        if (converter != null && !isClosed()) {
            List<String> children = zkNodeStore.getChildren();
            if (children == null) {
                convertedObj = null;
            } else {
                convertedObj = converter.convert(children);
            }
        }
    }

    @Override
    protected void onClose() {
        zkNodeStore.removeChildrenFetcher(this);
        lastSnapShot = zkNodeStore.getChildren();
    }

}
