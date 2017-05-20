package com.gome.fup.mq.common.exception;

/**
 * 服务地址未加载异常
 * Created by fupeng on 17/5/20.
 */
public class NoServerAddrException extends Exception {

    public NoServerAddrException(String msg) {
        super(msg);
    }
}
