package src.main.java.SystemIO;

import java.io.IOException;//用于处理IO异常
import java.net.InetSocketAddress;//表示IP地址和端口的套接字地址
import java.nio.channels.SocketChannel;//NIO中的套接字通道，用于非阻塞IO操作
import java.util.LinkedList;//链表用于存储客户端连接
//本代码是一个用于模拟大量客户端连接服务器的程序，主要通过SocketChannel创建大量TCP连接
public class C10KClient {

    public static void main(String[] args) {
        LinkedList<SocketChannel> clients = new LinkedList<>();
        //定义服务器的地址和端口号
        InetSocketAddress serverAddr = new InetSocketAddress("192.168.150.11", 9090);

        for (int i = 10000; i < 65000; i++) {
            //创建两个SocketChannel实例（NIO的套接字通道，支持非阻塞）
            try {
                SocketChannel client1 = SocketChannel.open();
                SocketChannel client2 = SocketChannel.open();
                //绑定client1到本地地址，IP为192.168.15.1，端口为当前循环变量i
                client1.bind(new InetSocketAddress("192.168.15.1", i));
                client1.connect(serverAddr);//让client1连接到目标服务器地址
                clients.add(client1);//将连接成功的client1加入链表
                client2.bind(new InetSocketAddress("192.168.110.100", i));
                client2.connect(serverAddr);
                clients.add(client2);
            } catch (IOException e) {//捕获IO异常，如连接失败，端口被占用等
                e.printStackTrace();
            }
        }
        //输出连接数量，并阻塞程序
        System.out.println("clients" + clients.size());
        try {
            System.in.read();//读物用户的输入，如果没有输入，则处于阻塞状态
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
