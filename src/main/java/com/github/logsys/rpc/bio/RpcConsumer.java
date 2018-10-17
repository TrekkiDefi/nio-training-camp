/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.github.logsys.rpc.bio;

import lombok.extern.slf4j.Slf4j;

/**
 * RpcConsumer
 *
 * @author william.liangf
 */
@Slf4j
public class RpcConsumer {

    public static void main(String[] args) throws Exception {
        HelloService service = RpcFramework.refer(HelloService.class, "127.0.0.1", 1234);
        for (int i = 0; i < 10; i++) {
            String hello = service.hello("World" + i);
            log.info(hello);
        }
    }

}
