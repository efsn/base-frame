package com.codeyn.resouce.bus.mq;

public interface IMsgHandler<T> {

    void handleMessage(T message);
    
}
