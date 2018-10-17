package com.github.logsys.rpc.nio;

/**
 * Created by liuchunlong on 2018/10/16.
 */
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
        return "Hello " + name;
    }

}
