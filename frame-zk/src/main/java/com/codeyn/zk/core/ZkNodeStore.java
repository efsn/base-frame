package com.codeyn.zk.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.codeyn.zk.fetcher.ChildrenFetcher;
import com.codeyn.zk.fetcher.DataFetcher;

public class ZkNodeStore {

    private String znodePath;
    private volatile byte[] data;
    private volatile List<String> children;

    private List<DataFetcher> dataFetchers = Collections.synchronizedList(new ArrayList<DataFetcher>());
    private List<ChildrenFetcher> childrenFetchers = Collections.synchronizedList(new ArrayList<ChildrenFetcher>());

    public ZkNodeStore(String znodePath) {
        this.znodePath = znodePath;
    }

    public String getZnodePath() {
        return znodePath;
    }

    public void setData(byte[] data) {
        this.data = data;

    }

    public void setChildren(List<String> children) {
        if (children != null) {
            children = Collections.unmodifiableList(children);
        }
        this.children = children;
    }

    public void notifyDataCallbacks() {
        for (Object df : dataFetchers.toArray()) {
            ((DataFetcher) df).onDataUpdate();
        }
    }

    public void notifyChildrenCallbacks() {
        for (Object df : childrenFetchers.toArray()) {
            ((ChildrenFetcher) df).onChildrenUpdate();
        }
    }

    public boolean hasDataFetcher() {
        return dataFetchers.size() > 0;
    }

    public boolean hasChildrenFetcher() {
        return childrenFetchers.size() > 0;
    }

    public List<String> getChildren() {
        return children;
    }

    public byte[] getData() {
        return data;
    }

    public void removeDataFetcher(DataFetcher df) {
        dataFetchers.remove(df);
    }

    public void removeChildrenFetcher(ChildrenFetcher cf) {
        childrenFetchers.remove(cf);
    }

    public void addDataFetcher(DataFetcher df) {
        dataFetchers.add(df);
    }

    public void addChildrenFetcher(ChildrenFetcher cf) {
        childrenFetchers.add(cf);
    }

}
