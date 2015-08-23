package com.codeyn.zk.fetcher;

public interface DataConverter<T> {

    public T convert(byte[] data);
}
