package com.github.logsys.rpc.nio;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by liuchunlong on 2018/10/16.
 */
public class RpcNioMultServerTask implements Runnable {

    private byte[] bytes;

    private SocketChannel channel;

    public RpcNioMultServerTask(byte[] bytes, SocketChannel channel) {
        this.bytes = bytes;
        this.channel = channel;
    }

    @Override
    public void run() {
        if (bytes != null && bytes.length > 0 && channel != null) {
            // 反序列化
            RequstMultObject requstMultObject = (RequstMultObject) SerializeUtil.unSerialize(bytes);
            // 调用服务并序列化结果然后返回
            requestHandle(requstMultObject, channel);
        }
    }

    public void requestHandle(RequstMultObject requstObject, SocketChannel channel) {

        Long requestId = requstObject.getRequestId(); // 请求ID
        Object obj = BeanContainer.getBean(requstObject.getCalzz()); // 根据请求类型，获取服务端的实现
        String methodName = requstObject.getMethodName(); // 请求的方法名称
        Class<?>[] parameterTypes = requstObject.getParamTypes(); // 请求的参数类型
        Object[] arguments = requstObject.getArgs(); // 请求的参数
        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            Object result = method.invoke(obj, arguments);
            byte[] bytes = SerializeUtil.serialize(result);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 12);
            // 为了便于客户端获得请求ID，直接将id写在头部（这样客户端直接解析即可获得，不需要将所有消息反序列化才能得到）
            // 然后写入消息题的长度，最后写入返回内容
            buffer.putLong(requestId);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.write(buffer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
            e.printStackTrace();
        }
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }
}
