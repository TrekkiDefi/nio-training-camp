package com.github.logsys.rpc.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by liuchunlong on 2018/10/17.
 */
@Slf4j
public class RpcNioMultClient {

    private static volatile RpcNioMultClient rpcNioClient;

    private Selector selector; // 多路复用器
    private SocketChannel channel; // Socket通道

    private String host = "localhost"; // 服务器IP
    private int port = 8080; // 服务器端口

    private RpcNioMultClient() {
        // 初始化client
        initClient();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                listen();
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    public static RpcNioMultClient getInstance() {
        if (rpcNioClient == null) {
            synchronized (RpcNioMultClient.class) {
                if (rpcNioClient == null) {
                    rpcNioClient = new RpcNioMultClient();
                }
            }
        }
        return rpcNioClient;
    }

    public void initClient() {
        try {
            // 创建Socket通道
            channel = SocketChannel.open();
            // 设置为异步非阻塞模式
            channel.configureBlocking(false);

            // 创建多路复用器，用于监听通道事件
            selector = Selector.open();

            // 建立连接
            channel.connect(new InetSocketAddress(host, port));
            // 判断此通道上是否正在进行连接操作。
            // 当且仅当已在此通道上发起连接操作，但是尚未通过调用 finishConnect 方法完成连接时才返回 true
            if (channel.isConnectionPending()) {
                while (!channel.finishConnect()) {
                    //wait, or do something else...
                }
            }

            // 如果直接连接成功，则注册到多路复用器，发送请求消息，并读取应答
//            if (channel.connect(new InetSocketAddress(host, port))) {
//                channel.register(selector, SelectionKey.OP_READ);
//            } else { // 直接连接失败，此通道处于非阻塞模式，并且连接操作正在进行中
//                channel.register(selector, SelectionKey.OP_CONNECT);
//            }

            log.info("客户端初始化完成，建立连接完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                selector.select();
                Iterator ite = selector.selectedKeys().iterator();
                while (ite.hasNext()) {
                    SelectionKey key = (SelectionKey) ite.next();
                    // 删除已选的key,以防重复处理
                    ite.remove();

                    if (key.isValid()) {

//                        SocketChannel socketChannel = (SocketChannel) key.channel();
//                        if (key.isConnectable()) { // 连接操作完成
//                            // 连接操作完成，即服务器返回了ACK应答信息。
//                            // 这时，我们需要对连接结果进行判断，调用socketChannel.finishConnect()，
//                            // 如果返回值true，说明连接成功；如果返回值false，说明正在进行连接；或者抛出IOException，说明连接失败
//                            if (socketChannel.finishConnect()) {
//                                socketChannel.register(selector, SelectionKey.OP_READ);
//                            }
//                        }

                        if (key.isReadable()) {
                            // 读取信息
                            readMsgFromServer();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.info("客户端建立连接失败");
        }
    }

    public boolean sendMsg2Server(byte[] bytes) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4 + bytes.length);
            // 放入消息长度，然后放入消息体
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            // 写出消息
            channel.write(buffer);
        } catch (IOException e) {
            log.info("客户端写出消息失败！");
            e.printStackTrace();
        }
        return true;
    }

    public void readMsgFromServer() {
        ByteBuffer byteBuffer;
        try {
            // 首先读取请求ID
            byteBuffer = ByteBuffer.allocate(8);
            int readIdCount = channel.read(byteBuffer);
            if (readIdCount < 0) {
                return;
            }
            byteBuffer.flip();
            Long requsetId = byteBuffer.getLong();

            // 读取返回值长度
            byteBuffer = ByteBuffer.allocate(4);
            int readHeadCount = channel.read(byteBuffer);
            if (readHeadCount < 0) {
                return;
            }
            byteBuffer.flip();
            int length = byteBuffer.getInt();

            // 读取消息体
            byteBuffer = ByteBuffer.allocate(length);
            int readBodyCount = channel.read(byteBuffer);
            if (readBodyCount < 0) {
                return;
            }
            byte[] bytes = byteBuffer.array();

            // 将返回值放入指定容器
            RpcContainer.addResponse(requsetId, bytes);
        } catch (IOException e) {
            log.info("读取数据异常");
            e.printStackTrace();
        }
    }
}
