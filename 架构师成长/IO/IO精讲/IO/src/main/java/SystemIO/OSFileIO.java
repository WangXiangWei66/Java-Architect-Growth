package src.main.java.SystemIO;

import org.junit.Test;//单元测试的注解

import java.io.BufferedOutputStream;//缓冲输出流
import java.io.File;//文件对象
import java.io.FileOutputStream;//文件输出流
import java.io.RandomAccessFile;//随机访问文件
import java.nio.ByteBuffer;//字节缓冲区
import java.nio.MappedByteBuffer;//内存映射缓冲区
import java.nio.channels.FileChannel;//文件通道
//演示了不同的文件I/O操作，包括基本文件I/O、缓冲文件I/O、随机访问文件一级对ByteBuffer的操作
public class OSFileIO {

    static byte[] data = "123456789\n".getBytes();
    static String path = "/root/testfileio/out.txt";

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "0":
                testBasicFileIO();
                break;
            case "1":
                testBufferedFileIO();
                break;
            case "2":
                testRandomAccessFileWrite();
            case "3":
//                whatByteBuffer();
            default:
        }
    }

    public static void testBasicFileIO() throws Exception {
        File file = new File(path);//指定路径文件
        FileOutputStream out = new FileOutputStream(file);//向文件写入数据
        while (true) {
            Thread.sleep(10);
            out.write(data);
        }
    }

    public static void testBufferedFileIO() throws Exception {
        File file = new File(path);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));//提供缓冲功能以提高写入的效率
        while (true) {
            Thread.sleep(10);
            out.write(data);
        }
    }

    public static void testRandomAccessFileWrite() throws Exception {
        RandomAccessFile raf = new RandomAccessFile(path, "rw");//以读写模式打开指定文件
        raf.write("hello mashibing\n".getBytes());
        raf.write("hello seanzhou\n".getBytes());
        System.out.println("write---------------");
        System.in.read();//用户输入
        raf.seek(4);
        raf.write("ooxx".getBytes());
        System.out.println("seek--------------");
        System.in.read();
        FileChannel rafChannel = raf.getChannel();
        MappedByteBuffer map = rafChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
        map.put("@@@".getBytes());
        System.out.println("map--put-----------");
        System.in.read();
        raf.seek(0);
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int read = rafChannel.read(buffer);//从文件通道读取数据到缓冲区
        System.out.println(buffer);
        buffer.flip();//切换缓冲区到读取模式
        System.out.println(buffer);
        //逐个读取并打印缓冲区的字符
        for (int i = 0; i < buffer.limit(); i++) {
            Thread.sleep(200);
            System.out.println(((char) buffer.get(i)));
        }
    }

    @Test
    public void whatByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //下面打印缓冲区的初始状态信息
        System.out.println("position: " + buffer.position());
        System.out.println("limit: " + buffer.limit());
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("mark: " + buffer);
        buffer.put("123".getBytes());
        System.out.println("---------put:123..........");
        System.out.println("mark: " + buffer);
        buffer.flip();
        System.out.println("--------------flip--------");
        System.out.println("mark: " + buffer);
        buffer.get();
        System.out.println("-----------------get--------");
        System.out.println("mark: " + buffer);
        buffer.compact();//压缩缓冲区
        System.out.println("---------compact......");
        System.out.println("mark: " + buffer);
        buffer.clear();
        System.out.println("----------clear.....");
        System.out.println("mark: " + buffer);
    }
}
