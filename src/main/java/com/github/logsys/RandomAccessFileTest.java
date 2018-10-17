package com.github.logsys;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Created by liuchunlong on 2018/10/13.
 */
@Slf4j
public class RandomAccessFileTest {

    @Test
    public void test() throws IOException {
        File fileA = new File("fileA.txt");
//        File fileB = new File("fileB.txt");
//        write(fileA);
//        read(fileA);
//        radRead(fileA);
//        copyFile(fileA, fileB);
        nioWrite(fileA);
        nioRead(fileA);
    }

    /**
     * 随机流读数据
     */
    private void read(File file) throws IOException {
        // 以 r 即只读的方法读取数据
        RandomAccessFile ras = new RandomAccessFile(file, "r");
        byte b = ras.readByte();
        log.info(String.valueOf(b)); //65

        int i = ras.readInt();
        log.info(String.valueOf(i)); //97

        String str = ras.readUTF(); //帅锅
        log.info(str);
        ras.close();
    }

    /**
     * 随机流写数据
     */
    private void write(File file) throws IOException {
        // 以 rw 即读写的方式写入数据
        RandomAccessFile ras = new RandomAccessFile(file, "rw");
        ras.writeByte(65);
        ras.writeInt(97);

        ras.writeUTF("帅锅");
        ras.close();
    }

    /**
     * 随机流读数据
     * <p>
     * 输出：65 1 帅锅 65
     */
    private void radRead(File file) throws IOException {
        // 以 r 即只读的方法读取数据
        RandomAccessFile ras = new RandomAccessFile(file, "r");

        byte b = ras.readByte();
        log.info(String.valueOf(b)); //65

        // 我们已经读取了一个字节的数据，那么当前偏移量为 1
        log.info(String.valueOf(ras.getFilePointer()));  //1
        // 这时候我们设置偏移量为5，那么可以直接读取后面的字符串（前面是一个字节 + 一个整型数据 = 5个字节）
        ras.seek(5);
        String str = ras.readUTF(); //帅锅
        log.info(str);

        // 这时我们设置偏移量为 0，那么从头开始
        ras.seek(0);
        log.info(String.valueOf(ras.readByte())); //65

        ras.close();
    }

    /**
     * 随机流复制文件
     *
     * @param fileA
     * @param fileB
     * @throws IOException
     */
    private void copyFile(File fileA, File fileB) throws IOException {
        RandomAccessFile srcRA = new RandomAccessFile(fileA, "rw");
        RandomAccessFile descRA = new RandomAccessFile(fileB, "rw");

        // 向文件 fileA.txt 中写入数据
        srcRA.writeByte(65);
        srcRA.writeInt(97);
        srcRA.writeUTF("帅锅");
        // 获取 fileA.txt 文件的字节长度
        // 需要注意的是：UTF 写入的数据默认会在前面增加两个字节的长度
        int len = (int) srcRA.length();
        log.info("fileA.txt length = " + len); //13

        srcRA.seek(0);
        log.info("fileA.txt text = " + srcRA.readByte() + ", " + srcRA.readInt() + ", " + srcRA.readUTF());

        // 开始复制
        srcRA.seek(0);
        // 定义一个字节数组，用来存放 fileA.txt 文件的数据
        byte[] buffer = new byte[len];
        // 将 fileA.txt 文件的内容读到 buffer 中
        srcRA.readFully(buffer); // 将 buffer.length 个字节从此文件读入 byte 数组，并从当前文件指针开始。
        // 再将 buffer 写入到 fileB.txt 文件中
        descRA.write(buffer);

        // 读取 fileB.txt 文件中的数据
        descRA.seek(0);
        log.info("fileB.txt text = " + descRA.readByte() + ", " + descRA.readInt() + ", " + descRA.readUTF());
        // 关闭流资源
        srcRA.close();
        descRA.close();
    }

    /**
     * 随机流写数据 nio
     *
     * @param file
     */
    private void nioWrite(File file) throws IOException {

        // 随机读写文件流创建的管道
        FileChannel fc = new RandomAccessFile(file, "rw").getChannel();
        // fc.position()计算从文件的开始到当前位置之间的字节数
        log.info("The channel position = " + fc.position());
        // 设置此通道的文件位置，fc.size()此通道的文件的当前大小，该条语句执行后，通道位置处于文件的末尾
//        fc.position(fc.size());
        // 在文件末尾写入字节
        fc.write(ByteBuffer.wrap("Some more".getBytes()));
        fc.close();
    }

    /**
     * 随机流读数据 nio
     *
     * @param file
     */
    private void nioRead(File file) throws IOException {
        FileChannel fc = new RandomAccessFile(file, "r").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
        // 将文件内容读到指定的缓冲区中
        fc.read(buffer);
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        if (!buffer.hasRemaining()) {
            log.info(new String(bytes, Charset.defaultCharset()));
        }
        fc.close();
    }
}
