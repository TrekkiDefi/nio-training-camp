package com.github.logsys.rpc.nio;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by liuchunlong on 2018/10/17.
 */
@Slf4j
public class RpcNIoMultHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 生成请求ID
        Long responseId = RpcContainer.getRequestId();

        // 封装请求对象
        RequestMultObject requestMultObject = new RequestMultObject(method.getDeclaringClass(), method.getName(),
                method.getParameterTypes(), args);
        requestMultObject.setRequestId(responseId);

        // 封装设置rpcResponseFuture，主要用于获取返回值
        RpcResponseFuture rpcResponseFuture = new RpcResponseFuture(responseId);
        RpcContainer.addRequstFuture(rpcResponseFuture);

        // 序列化
        byte[] requsetBytes = SerializeUtil.serialize(requestMultObject);
        // 发送请求信息
        RpcNioMultClient rpcNioMultClient = RpcNioMultClient.getInstance();
        rpcNioMultClient.sendMsg2Server(requsetBytes);

        // 从ResponseContainer获取返回值
        byte[] responseBytes = rpcResponseFuture.get();
        if (requsetBytes != null) {
            RpcContainer.removeResponseAndFuture(responseId);
        }

        // 反序列化获得结果
        Object result = SerializeUtil.unSerialize(responseBytes);
        log.info("请求id：" + responseId + " 结果：" + result);
        return result;
    }
}
