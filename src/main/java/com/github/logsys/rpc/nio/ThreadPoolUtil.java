package com.github.logsys.rpc.nio;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuchunlong on 2018/10/16.
 */
public class ThreadPoolUtil {

    private static volatile ThreadPoolExecutor executor;

    public static void init() {
        if (executor == null) {
            synchronized (ThreadPoolUtil.class) {
                executor = new ThreadPoolExecutor(10, 20, 200, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
            }
        }
    }

    public static void addTask(RpcNioMultServerTask task) {
        if (executor == null) {
            init();
        }
        executor.execute(task);
    }
}
