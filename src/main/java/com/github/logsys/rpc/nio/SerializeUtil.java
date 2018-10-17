package com.github.logsys.rpc.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Created by liuchunlong on 2018/10/16.
 * <p>
 * 序列化工具
 */
@Slf4j
public class SerializeUtil {

    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            log.info("序列化对象出错！");
            e.printStackTrace();
            return null;
        }
    }

    public static Object unSerialize(byte[] bytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.info("反序列化出错！");
            e.printStackTrace();
            return null;
        }
    }
}
