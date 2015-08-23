package com.codeyn.zk.fetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codeyn.zk.core.ZkNodeStore;

public abstract class ChildrenDataFetcher extends AbstractFetcher {

    private ChildrenFetcher childrenFetcher;
    private Map<String, DataFetcher> dataFetchers = new HashMap<>();

    public ChildrenDataFetcher(ZkNodeStore zkNodeStore) {
        super(zkNodeStore);
        this.childrenFetcher = new ChildrenFetcher(zkNodeStore) {
            @Override
            public synchronized void onChildrenUpdate() {
                super.onChildrenUpdate();
                updateChildren();
            }
        };
        updateChildren();
        zkNodeStore.addChildrenFetcher(childrenFetcher);
    }

    public List<String> getNodeChildren() {
        return childrenFetcher.getNodeChildren();
    }

    public boolean contains(String child) {
        return childrenFetcher.contains(child);
    }

    public byte[] getChildData(String child) {
        DataFetcher dataFetcher = dataFetchers.get(child);
        if (dataFetcher == null) {
            return null;
        } else {
            return dataFetcher.getDataBytes();
        }
    }

    public Object getChildDataConvertedObject(String child) {
        DataFetcher dataFetcher = dataFetchers.get(child);
        if (dataFetcher == null) {
            return null;
        } else {
            return dataFetcher.getConvertedObject();
        }
    }

    private void updateChildren() {
        List<String> newChildren = childrenFetcher.getNodeChildren();
        Map<String, DataFetcher> newMap = new HashMap<>();
        if (newChildren != null) {
            for (String child : newChildren) {
                if (dataFetchers.containsKey(child)) {
                    newMap.put(child, dataFetchers.get(child));
                } else {
                    String childPath = getZnodePath() + "/" + child;
                    DataFetcher df = createDataFetcher(childPath);
                    newMap.put(child, df);
                }
            }
        }
        Map<String, DataFetcher> oldMap = dataFetchers;
        dataFetchers = newMap;
        for (String oldChild : oldMap.keySet()) {
            if (!dataFetchers.containsKey(oldChild)) {
                oldMap.get(oldChild).close();
            }
        }
    }

    @Override
    protected void onClose() {
        childrenFetcher.close();
        for (DataFetcher df : dataFetchers.values()) {
            df.close();
        }
    }

    protected abstract DataFetcher createDataFetcher(String childPath);
}
