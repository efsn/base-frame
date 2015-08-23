package com.codeyn.zk.core;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultZkWatcher implements Watcher {

    private ZkEngine zkEngine;

    private CountDownLatch connectedSingal;

    public DefaultZkWatcher(ZkEngine zkEngine, CountDownLatch connectedSingal) {
        this.zkEngine = zkEngine;
        this.connectedSingal = connectedSingal;
    }

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(DefaultZkWatcher.class);

    /**
     * 新增、删除注册服务通知回调.
     */
    @Override
    public void process(WatchedEvent event) {

        // 连接状态
        KeeperState keeperState = event.getState();
        // 事件类型
        EventType eventType = event.getType();
        // 受影响的path
        String path = event.getPath();

        logger.debug("keeperState is " + keeperState + ", eventType is " + eventType + ", path is " + path);

        try {
            if (eventType == EventType.None) {
                // 连接成功
                if (keeperState == Event.KeeperState.SyncConnected) {
                    if (connectedSingal != null) {
                        connectedSingal.countDown();
                        connectedSingal = null;
                    }
                    zkEngine.connectedCallback();
                }
                // 连接中断
                else if (keeperState == Event.KeeperState.Disconnected) {
                    zkEngine.disconnectedCallback();
                }
                // 会话过期
                else if (keeperState == Event.KeeperState.Expired) {
                    zkEngine.expiredCallback();
                }
            } else if (keeperState == Event.KeeperState.SyncConnected) {
                // 注册服务
                if (EventType.NodeCreated == eventType) {
                    zkEngine.nodeCreatedCallback(path);
                }
                // 注销服务
                else if (EventType.NodeDeleted == eventType) {
                    zkEngine.nodeDeleteCallback(path);
                }
                // 子节点变化
                else if (EventType.NodeChildrenChanged == eventType) {
                    zkEngine.childChangeCallback(path);
                }
                // 节点数据变化
                else if (EventType.NodeDataChanged == eventType) {
                    zkEngine.dataChangeCallback(path);
                }
            }
        } catch (Exception e) {
            // 回调通知的异常直接捕获，不对上抛出了
            logger.error("Some error happened on zk callback.keeperState is " + keeperState + ", eventType is "
                    + eventType + ", path is " + path, e);
        }
    }

}
