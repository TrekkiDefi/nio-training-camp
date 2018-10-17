package com.github.logsys.rpc.nio;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liuchunlong on 2018/10/17.
 */
@Slf4j
public class RpcResponseFuture {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private Long requsetId;

    /**
     *
     * @param requsetId 请求ID
     */
    public RpcResponseFuture(Long requsetId) {
        this.requsetId = requsetId;
    }

    public byte[] get() {
        // 获取响应结果
        byte[] bytes = RpcContainer.getResponse(requsetId);
        if (bytes == null || bytes.length < 0) {
            lock.lock();
            try {
                log.info("请求id:" + requsetId + ",请求结果尚未返回，线程挂起");
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        log.info("请求id:" + requsetId + ",请求结果返回，线程挂起结束");
        return RpcContainer.getResponse(requsetId);
    }

    public void rpcIsDone() {
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public Long getRequstId() {
        return requsetId;
    }

    public void setRequstId(Long requsetId) {
        this.requsetId = requsetId;
    }
}
