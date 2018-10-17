package com.github.logsys.rpc.nio;

import java.lang.reflect.Proxy;

/**
 * Created by liuchunlong on 2018/10/17.
 */
public class RpcProxyFactory {
    /**
     * 多线程环境代理对象
     *
     * @param interfaceClass
     * @return T
     */
    public static <T> T getMultService(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                new RpcNIoMultHandler());
    }
}
