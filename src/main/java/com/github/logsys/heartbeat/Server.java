package com.github.logsys.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by liuchunlong on 2018/10/14.
 * <p>
 * 长连接 & 心跳机制，nio实现方式
 */
@Slf4j
public class Server {

    private Map<String, Long> heatTimeMap = new HashMap<>();

    volatile int count = 0;

    public Server(int port) {

        Selector selector = null; // 多路复用器
        ServerSocketChannel serverChannel = null; // ServerSocket通道

        try {

            // 创建一个ServerSocket通道
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false); // 异步非阻塞
            serverChannel.socket().bind(new InetSocketAddress(port));

            // 创建多路复用器
            selector = Selector.open();

            // 将通道与多路复用器绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，
            // 只有当该事件到达时，Selector.select()会返回，否则一直阻塞。
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {

                // 选择注册过的IO操作的事件
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey readyKey = it.next();
                    // 删除已选key，防止重复处理
                    it.remove();
                    if (readyKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) readyKey.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (readyKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) readyKey.channel();

                        Object obj = receiveData(socketChannel);
                        if (obj != null) {
                            count++;
                            log.info("Server received: " + obj.toString() + ", count: " + count);
                            String msg = "Server responsed: ";
                            if (obj instanceof String) {
                                String id = obj.toString().split(",")[0]; // 请求客户端id
                                if (heatTimeMap.get(id) != null
                                        && System.currentTimeMillis() - heatTimeMap.get(id) > 60 * 1000) {
                                    socketChannel.socket().close();
                                } else {
                                    heatTimeMap.put(id, System.currentTimeMillis());
                                }
                                long time = System.currentTimeMillis();
                                msg += time + "$";
                                sendData(socketChannel, msg);
                            } else if (obj instanceof Pojo) {
                                msg += ((Pojo) obj).getName() + "$";
                                sendData(socketChannel, msg);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                selector.close();
                if (serverChannel != null) {
                    serverChannel.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Object receiveData(SocketChannel socketChannel) {
        Object obj = null;

        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        ByteBuffer objBuffer;

        int size = 0;
        int sum = 0;
        byte[] bytes = null;

        int objlen = 0; // 消息长度

        try {
            if ((size = socketChannel.read(intBuffer)) > 0) { // 读取代表消息长度的前4个字节
                intBuffer.flip();
                bytes = new byte[size];
                intBuffer.get(bytes);
                intBuffer.clear();
                if (bytes.length == 4) {
                    objlen = bytesToInt(bytes, 0); // 消息长度
                }
                if (objlen > 0) {
                    byte[] objByte = new byte[0];
                    objBuffer = ByteBuffer.allocate(objlen); // 通过objlen巧妙的解决了粘包的问题
                    size = socketChannel.read(objBuffer);
                    if (size > 0) {
                        objBuffer.flip();
                        bytes = new byte[size];
                        objBuffer.get(bytes, 0, size);
                        objBuffer.clear();
                        objByte = ArrayUtils.addAll(objByte, bytes);
                        sum += bytes.length;
                    }
                    obj = ByteToObject(objByte);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static void sendData(SocketChannel socketChannel, Object obj) {
        byte[] bytes = ObjectToByte(obj);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将byte数组中从指定offet位置开始连续的4个byte转换为int，
     * <p>
     * 本方法适用于低位在前，高位在后顺序的byte数组。
     *
     * @param ary    byte数组
     * @param offset 从数组的第offset位开始
     * @return
     */
    public static int bytesToInt(byte[] ary, int offset) {
        /*
            0xFF：                                  11111111
            0xFF00：                       11111111 00000000
            0xFF0000：            11111111 00000000 00000000
            0xFF000000： 11111111 00000000 00000000 00000000
         */
        int value;
        value = (ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000);
        return value;
    }

    /**
     * 将字节数组转换为Object对象
     *
     * @param bytes
     * @return
     */
    public static Object ByteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return obj;
    }

    /**
     * 将Object对象转换为字节数组
     *
     * @param obj
     * @return
     */
    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void main(String[] args) {
        Server server = new Server(55555);
    }
}
