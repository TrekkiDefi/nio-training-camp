package com.github.logsys.rpc.nio;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by liuchunlong on 2018/10/17.
 */
@Slf4j
public class RpcNioConsumer {

    public static void main(String[] args) {
        multipartRpcNio();
    }

    /**
     * 多线程IO调用示例
     *
     * @param
     * @return void
     */
    public static void multipartRpcNio() {
        HelloService proxy = RpcProxyFactory.getMultService(HelloService.class);
        for (int i = 0; i < 100; i++) {
            int j = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String result = proxy.hello("world!");
                    log.info(j + result);
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
