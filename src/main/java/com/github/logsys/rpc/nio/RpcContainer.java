package com.github.logsys.rpc.nio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liuchunlong on 2018/10/17.
 */
public class RpcContainer {

    // 返回值容器
    private static ConcurrentHashMap<Long, byte[]> responseContainer = new ConcurrentHashMap<>();
    // 请求对象容器
    private static ConcurrentHashMap<Long, RpcResponseFuture> requestFuture = new ConcurrentHashMap<>();
    // 请求ID
    private static AtomicLong requstId = new AtomicLong(0);

    /**
     * 获取下一请求ID
     *
     * @return
     */
    public static Long getRequestId() {
        return requstId.getAndIncrement();
    }

    public static void addResponse(Long requestId, byte[] responseBytes) {
        responseContainer.put(requestId, responseBytes);
        RpcResponseFuture responseFuture = requestFuture.get(requestId);
        responseFuture.rpcIsDone();
    }

    /**
     * 获取响应结果
     *
     * @param requestId 请求ID
     * @return
     */
    public static byte[] getResponse(Long requestId) {
        return responseContainer.get(requestId);
    }

    public static void addRequstFuture(RpcResponseFuture rpcResponseFuture) {
        requestFuture.put(rpcResponseFuture.getRequstId(), rpcResponseFuture);
    }

    public static RpcResponseFuture getRpcRequstFutue(Long requestId) {
        return requestFuture.get(requestId);
    }

    /**
     * 移除指定请求
     *
     * @param requestId 请求ID
     */
    public static void removeResponseAndFuture(Long requestId) {
        responseContainer.remove(requestId);
        requestFuture.remove(requestId);
    }
}
