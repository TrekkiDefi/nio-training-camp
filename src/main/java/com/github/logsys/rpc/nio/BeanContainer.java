package com.github.logsys.rpc.nio;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuchunlong on 2018/10/17.
 */
public class BeanContainer {

    private static ConcurrentHashMap<Class<?>, Object> container = new ConcurrentHashMap<>();

    public static boolean addBean(Class<?> clazz, Object object) {
        container.put(clazz, object);
        return true;
    }

    public static Object getBean(Class<?> clazz) {
        return container.get(clazz);
    }
}
