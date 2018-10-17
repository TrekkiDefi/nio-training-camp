package com.github.logsys.rpc.nio;

import java.io.IOException;

/**
 * Created by liuchunlong on 2018/10/17.
 */
public class RpcNioProvider {

    public static void main(String[] args) throws IOException {

        // 将服务实现放进bean容器
        HelloService helloService = new HelloServiceImpl();
        BeanContainer.addBean(HelloService.class, helloService);
        // 启动NIO服务端
        startMultRpcNioServer();
    }

    public static void startMultRpcNioServer() {
        Runnable r = () -> {
            try {
                RpcNioMultServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}
