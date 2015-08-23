package com.codeyn.zk.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ZkContextCache {

    private ConcurrentHashMap<String, ZkNode> tempRegisterNodes = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ZkNodeStore> nodeStores = new ConcurrentHashMap<>();

    public ZkNode getCachedNode(String path) {
        return tempRegisterNodes.get(path);
    }

    public ZkNodeStore getNodeStore(String path) {
        return getNodeStore(path, true);
    }

    public ZkNodeStore getNodeStore(String path, boolean create) {
        ZkNodeStore store = nodeStores.get(path);
        if (store == null && create) {
            store = new ZkNodeStore(path);
            ZkNodeStore old = nodeStores.putIfAbsent(path, store);
            if (old != null) {
                store = old;
            }
        }
        return store;
    }

    public void cacheNode(ZkNode node) {
        tempRegisterNodes.put(node.getPath(), node);
    }

    public void removeNode(String path) {
        tempRegisterNodes.remove(path);
    }

    public List<ZkNode> getAllCachedNodes() {
        return new ArrayList<>(tempRegisterNodes.values());
    }

    public List<String> getAllNodeStorePaths() {
        return new ArrayList<>(nodeStores.keySet());
    }
}
