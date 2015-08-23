package com.codeyn.zk.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeyn.zk.exception.InvalidZnodePathException;
import com.codeyn.zk.exception.ZookeeperException;
import com.codeyn.zk.fetcher.ChildrenConverter;
import com.codeyn.zk.fetcher.ChildrenDataFetcher;
import com.codeyn.zk.fetcher.ChildrenFetcher;
import com.codeyn.zk.fetcher.DataConverter;
import com.codeyn.zk.fetcher.DataFetcher;

public class ZkEngine {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(ZkEngine.class);

    /** 连接ZooKeeper超时时间，默认20秒 */
    public static final int CONNECTION_TIMEOUT = 20 * 1000;

    /** 连接ZooKeeper超时时间 */
    public int connectionTimeout;

    /** The zk client. */
    private ZooKeeper zk;

    /** ZK 连接状态标志位 */
    private volatile boolean isConnected = false;

    private volatile boolean isClose = false;

    /** 缓存对象 */
    private ZkContextCache zkContextCache = new ZkContextCache();

    /** ZK地址 每个地址用ip:port格式表示，多个地址用逗号","分隔 */
    private String hosts = null;

    public ZkEngine(String hosts) {
        this(hosts, CONNECTION_TIMEOUT);
    }

    public ZkEngine(String hosts, int connectionTimeout) {
        if (null == hosts) {
            throw new NullPointerException("zk hosts is Null ");
        }
        this.hosts = hosts;
        this.connectionTimeout = connectionTimeout;
        initZk();
    }

    public boolean isClose() {
        return isClose;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void initZk() {
        CountDownLatch connectedSingal = new CountDownLatch(1);
        // 创建zk对象后会立即返回,这里的GlobalZkWatcher是全局watcher
        try {
            if (null != zk) {
                try {
                    zk.close();
                } catch (Exception e) {
                    logger.error("Close ZooKeeper failed", e);
                }
            }
            zk = new ZooKeeper(hosts, connectionTimeout, new DefaultZkWatcher(this, connectedSingal));
        } catch (IOException e) {
            throw new RuntimeException("Create ZooKeeper object failed.", e);
        }
        logger.info("Create ZooKeeper object succees. Use hosts: " + hosts);
        // 第一次建立连接同步等待一段时间，即使这次未成功，zk会接着反复重试连接
        try {
            isConnected = connectedSingal.await(connectionTimeout + 5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (!isConnected) {
            logger.error("First connect zookeeper failed.");
        }
    }

    /**
     * 关闭zookeeper连接.
     */
    public synchronized void close() {
        isClose = true;
        if (null != zk) {
            try {
                zk.close();
            } catch (InterruptedException e) {
            }
            zk = null;
            logger.info("Close ZooKeeper Connection");
        }
    }

    public String getPathWithSeesionId(String sourcePath) {
        if (sourcePath == null || sourcePath.trim().length() == 0) {
            return sourcePath;
        }
        return sourcePath + ":" + zk.getSessionId();
    }

    public boolean exist(String znodePath) throws ZookeeperException {
        if (!isConnected) {
            throw new ZookeeperException("Check zk node exist failed ,zk is disconnected. zkNode path: " + znodePath);
        }
        try {
            znodePath = checkNodePath(znodePath);
            return zk.exists(znodePath, false) != null;
        } catch (Exception e) {
            throw new ZookeeperException("Check zk node exist failed. zkNode path: " + znodePath, e);
        }
    }

    public void createNode(String znodePath) throws ZookeeperException {
        createNode(znodePath, null, false);
    }

    public void createNode(String znodePath, byte[] data) throws ZookeeperException {
        createNode(znodePath, data, false);
    }

    public void createNode(String znodePath, boolean isTemp) throws ZookeeperException {
        createNode(znodePath, null, isTemp);
    }

    /**
     * 注册一个节点
     * 如果是临时节点zk连接中断再重新恢复后会重新注册
     * 
     * @param zkNode
     *            节点信息
     * @throws ZookeeperException
     *             若未连接或连接中断，抛出异常
     */
    public void createNode(String znodePath, byte[] data, boolean isTemp) throws ZookeeperException {
        if (!isConnected) {
            throw new ZookeeperException("Create zk node failed ,zk is disconnected. zkNode path: " + znodePath);
        }
        try {
            znodePath = checkNodePath(znodePath);
            if (isTemp) {
                //znodePath = znodePath + ":" + zk.getSessionId();
                // 注册临时性节点，随着链接关闭而删除,父节点必须存在且为持久节点
                zk.create(znodePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zkContextCache.cacheNode(new ZkNode(znodePath, data));
                logger.info("Created temp zk node " + znodePath);
            } else {
                // 注册永久节点，关闭连接后不删除，父节点不存在则自动递归创建
                createPersistentNode(znodePath, data, true);
            }
        } catch (Exception e) {
            throw new ZookeeperException("Create zk node failed. zkNode path: " + znodePath, e);
        }
    }

    private void createPersistentNode(String path, byte[] data, boolean isFirst) throws KeeperException,
            InterruptedException {
        if (zk.exists(path, false) == null) {
            int i = path.lastIndexOf('/');
            if (i > 0) {
                createPersistentNode(path.substring(0, i), null, false);
            }
            zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            logger.info("Created zk node " + path);
        } else if (isFirst) {
            throw new KeeperException.NodeExistsException(path);
        }
    }

    /**
     * 删除一个节点
     * 
     * @param znodePath 节点路径
     * @throws ZookeeperException
     *             若未连接或连接中断，抛出异常
     */
    public void removeNode(String znodePath) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        if (!isConnected) {
            throw new ZookeeperException("Remove zk node failed ,zk is disconnected. zkNode path: " + znodePath);
        }
        try {
            zkContextCache.removeNode(znodePath);
            if (zk.exists(znodePath, false) != null) {
                zk.delete(znodePath, -1);
            }
            logger.info("Removed zkNode path " + znodePath);
        } catch (Exception e) {
            throw new ZookeeperException("Remove zk node failed. zkNode path: " + znodePath, e);
        }
    }

    public void updateNode(String znodePath, byte[] data) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        if (!isConnected) {
            throw new ZookeeperException("Update zk node data failed ,zk is disconnected. zkNode path: " + znodePath);
        }
        try {
            ZkNode node = zkContextCache.getCachedNode(znodePath);
            if (node != null) {
                node.setData(data);
            }
            zk.setData(znodePath, data, -1);
            logger.info("Updated zk node data for " + znodePath);
        } catch (Exception e) {
            throw new ZookeeperException("Update zk node data failed. zkNode path: " + znodePath, e);
        }
    }

    public void createOrUpdateNode(String znodePath, byte[] data, boolean isTemp) throws ZookeeperException {
        if (!isConnected) {
            throw new ZookeeperException("Create or Update zk node failed ,zk is disconnected. zkNode path: "
                    + znodePath);
        }
        try {
            if (zk.exists(znodePath, false) == null) {
                createNode(znodePath, data, isTemp);
            } else {
                updateNode(znodePath, data);
            }
        } catch (Exception e) {
            throw new ZookeeperException("Create or Update zk node failed. zkNode path: " + znodePath, e);
        }
    }

    public List<String> getNodeChildren(String znodePath) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        return doGetNodeChildren(znodePath, false);
    }

    public byte[] getNodeData(String znodePath) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        return doGetNodeData(znodePath, false);
    }

    public String getNodeDataAsString(String znodePath) throws ZookeeperException {
        return getNodeDataAsString(znodePath, "UTF-8");
    }

    public String getNodeDataAsString(String znodePath, String charset) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        byte[] data = doGetNodeData(znodePath, false);
        if (data == null) {
            return null;
        }
        try {
            return new String(data, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChildrenFetcher watchNodeChildren(String znodePath) throws ZookeeperException {
        return watchNodeChildren(znodePath, null);
    }

    public ChildrenFetcher watchNodeChildren(String znodePath, ChildrenConverter<?> converter)
            throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        ZkNodeStore nodeStore = zkContextCache.getNodeStore(znodePath);
        List<String> children = doGetNodeChildren(znodePath, true);
        nodeStore.setChildren(children);
        ChildrenFetcher fetcher = new ChildrenFetcher(nodeStore, converter);
        fetcher.onChildrenUpdate();
        nodeStore.addChildrenFetcher(fetcher);
        return fetcher;
    }

    public DataFetcher watchNodeData(String znodePath) throws ZookeeperException {
        return watchNodeData(znodePath, null);
    }

    public DataFetcher watchNodeData(String znodePath, DataConverter<?> converter) throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        ZkNodeStore nodeStore = zkContextCache.getNodeStore(znodePath);
        byte[] data = doGetNodeData(znodePath, true);
        nodeStore.setData(data);
        DataFetcher fetcher = new DataFetcher(nodeStore, converter);
        fetcher.onDataUpdate();
        nodeStore.addDataFetcher(fetcher);
        return fetcher;
    }

    public ChildrenDataFetcher watchNodeChildrenData(String znodePath) {
        return watchNodeChildrenData(znodePath, null);
    }

    public ChildrenDataFetcher watchNodeChildrenData(String znodePath, final DataConverter<?> converter)
            throws ZookeeperException {
        znodePath = checkNodePath(znodePath);
        ZkNodeStore nodeStore = zkContextCache.getNodeStore(znodePath);
        List<String> children = doGetNodeChildren(znodePath, true);
        nodeStore.setChildren(children);
        ChildrenDataFetcher fetcher = new ChildrenDataFetcher(nodeStore) {
            @Override
            protected DataFetcher createDataFetcher(String childPath) {
                return watchNodeData(childPath, converter);
            }
        };
        return fetcher;
    }

    private byte[] doGetNodeData(String znodePath, boolean watch) throws ZookeeperException {
        if (!isConnected) {
            throw new ZookeeperException("Fetch zk node data failed, zk is disconnected. znodePath : " + znodePath);
        }
        byte[] data = null;
        try {
            if (zk.exists(znodePath, watch) != null) {
                data = zk.getData(znodePath, watch, null);
                logger.info("Fetch zk node data success. znodePath: {}, data bytes count is {}", znodePath,
                        data == null ? 0 : data.length);
            }
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new ZookeeperException("Fetch zk node data failed. znodePath: " + znodePath, e);
        }
        return data;
    }

    private List<String> doGetNodeChildren(String znodePath, boolean watch) throws ZookeeperException {
        if (!isConnected) {
            throw new ZookeeperException("Fetch zk node children failed, zk is disconnected. znodePath : " + znodePath);
        }
        List<String> childrenList = new ArrayList<>(0);
        try {
            if (zk.exists(znodePath, watch) != null) {
                childrenList = zk.getChildren(znodePath, watch);
                logger.info("Fetch zk node children success. znodePath: {}, children count is {}", znodePath,
                        childrenList.size());
            }
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new ZookeeperException("Fetch zk node children failed. znodePath: " + znodePath, e);
        }
        return childrenList;
    }

    /**
     * 成功连接zookeeper回调
     */
    public void connectedCallback() {
        this.isConnected = true;
        logger.info("[zk callback] zk is connected. refresh temp nodes and fetchers");

        // 连接成功后，做一些恢复工作，重新注册以及刷新缓存
        List<ZkNode> cachedNodes = zkContextCache.getAllCachedNodes();
        for (ZkNode zkNode : cachedNodes) {
            try {
                this.createNode(zkNode.getPath(), zkNode.getData(), true);
            } catch (ZookeeperException e) {
                logger.error(e.getMessage(), e.getCause());
            }
        }
        List<String> nodeStorePaths = zkContextCache.getAllNodeStorePaths();
        for (String nodePath : nodeStorePaths) {
            refreshChildrenFetcher(nodePath);
            refreshDataFetcher(nodePath);
        }
    }

    /**
     * 断开连接回调
     */
    public void disconnectedCallback() {
        this.isConnected = false;
        logger.info("[zk callback] zk is disconnected.");
    }

    /**
     * 连接过期回调
     */
    public synchronized void expiredCallback() {
        this.isConnected = false;
        logger.info("zk is expired.");
        if (!isClose) {
            logger.info("[zk callback] reset zk instance.");
            // 会话已过期，重置zk
            initZk();
        }
    }

    public void nodeCreatedCallback(String path) {
        logger.info("[zk callback] Created znode path " + path);
        refreshChildrenFetcher(path);
        refreshDataFetcher(path);
    }

    public void nodeDeleteCallback(String path) {
        logger.info("[zk callback] Delete znode path " + path);
        ZkNodeStore zkNodeStore = zkContextCache.getNodeStore(path, false);
        if (zkNodeStore != null) {
            zkNodeStore.setChildren(null);
            zkNodeStore.setData(null);
            zkNodeStore.notifyChildrenCallbacks();
            zkNodeStore.notifyDataCallbacks();
        }
    }

    /**
     * 子节点变化通知
     */
    public void childChangeCallback(String path) {
        logger.info("[zk callback] Child changed for znode path " + path);
        refreshChildrenFetcher(path);
    }

    /**
     * 数据变化通知
     */
    public void dataChangeCallback(String path) {
        logger.info("[zk callback] Data changed for znode path " + path);
        refreshDataFetcher(path);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    private void refreshChildrenFetcher(String zkPath) {
        ZkNodeStore zkNodeStore = zkContextCache.getNodeStore(zkPath, false);
        if (zkNodeStore == null) {
            return;
        }
        if (zkNodeStore.hasChildrenFetcher()) {
            try {
                List<String> children = doGetNodeChildren(zkPath, true);
                zkNodeStore.setChildren(children);
                zkNodeStore.notifyChildrenCallbacks();
                logger.info("refresh children fetcher for znodePath: {}", zkPath);
            } catch (ZookeeperException e) {
                logger.error("refresh children fetcher failed! znodePath: {}", zkPath, e);
            }
        } else {
            zkNodeStore.setChildren(null);
        }
    }

    private void refreshDataFetcher(String zkPath) {
        ZkNodeStore zkNodeStore = zkContextCache.getNodeStore(zkPath, false);
        if (zkNodeStore == null) {
            return;
        }
        if (zkNodeStore.hasDataFetcher()) {
            try {
                byte[] data = doGetNodeData(zkNodeStore.getZnodePath(), true);
                zkNodeStore.setData(data);
                zkNodeStore.notifyDataCallbacks();
                logger.info("refresh data fetcher for znodePath: {}", zkPath);
            } catch (ZookeeperException e) {
                logger.error("refresh data fetcher failed! znodePath: {}", zkPath, e);
            }
        } else {
            zkNodeStore.setData(null);
        }
    }

    private String checkNodePath(String znodePath) {
        if (znodePath == null || znodePath.length() == 0 || "/".equals(znodePath)) {
            throw new InvalidZnodePathException("Invalid zk node path: " + znodePath);
        }
        if (znodePath.endsWith("/")) {
            znodePath = znodePath.substring(0, znodePath.length() - 1);
        }
        if (!znodePath.startsWith("/")) {
            znodePath = "/" + znodePath;
        }
        return znodePath;
    }
}
