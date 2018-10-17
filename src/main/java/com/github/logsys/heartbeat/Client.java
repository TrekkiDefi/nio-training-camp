package com.github.logsys.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by liuchunlong on 2018/10/14.
 * <p>
 * 长连接 & 心跳机制，nio实现方式
 */
@Slf4j
public class Client {

    private Socket socket;

    private String id;

    DataOutputStream dos;

    DataInputStream dis;

    public Client(String ip, int port, String id) {
        try {
            this.id = id;
            this.socket = new Socket(ip, port);
            this.socket.setKeepAlive(true);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
//            new Thread(new HeartThread()).start(); // 心跳检测
            new Thread(new MsgThread()).start(); // 发送消息
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Object content) {
        try {
            int len = ObjectToByte(content).length;

            ByteBuffer dataLenBuf = ByteBuffer.allocate(4);
            dataLenBuf.order(ByteOrder.LITTLE_ENDIAN); // 低位优先
            dataLenBuf.putInt(0, len);

            dos.write(dataLenBuf.array(), 0, 4);
            dos.flush();
            dos.write(ObjectToByte(content));
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    public void closeSocket() {
        try {
            socket.close();
            dos.close();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
        return obj;
    }

    class HeartThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    long time = System.currentTimeMillis();
                    sendMsg("CLIENT" + id + ", " + time + " END.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MsgThread implements Runnable {
        @Override
        public void run() {

            int temp;
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((temp = dis.read()) != -1) {
                        bytes[len] = (byte) temp;
                        len++;
                        if ((char) temp == '$')
                            break;
                    }
                    String resp = ByteToObject(ArrayUtils.subarray(bytes, 0, len)).toString();
                    log.info(resp.substring(0, resp.length() - 1));
                } catch (Exception e) {
                    closeSocket();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 55555, "1");
        int i;
        for(i = 0; i < 50; i++) {
            client.sendMsg(new Pojo("hutu92", 26, new ArrayList<String>()));
        }
        log.info("Send msg count: " + i);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Client("127.0.0.1", 55555, "2");
    }
}
