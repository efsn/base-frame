package com.codeyn.zk.fetcher;

import java.util.List;

public interface ChildrenConverter<T> {

    public List<T> convert(List<String> newChildren);
}
