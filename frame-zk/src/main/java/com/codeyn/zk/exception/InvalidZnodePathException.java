package com.codeyn.zk.exception;

public class InvalidZnodePathException extends ZookeeperException {

    private static final long serialVersionUID = 1L;

    public InvalidZnodePathException(String message) {
        super(message);
    }
}
