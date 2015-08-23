package com.codeyn.zk.exception;

public class ZookeeperException extends RuntimeException {

    private static final long serialVersionUID = -1605910805363981432L;

    public ZookeeperException(String message) {
        super(message);
    }

    public ZookeeperException() {
        super();
    }

    public ZookeeperException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZookeeperException(Throwable cause) {
        super(cause);
    }

}
