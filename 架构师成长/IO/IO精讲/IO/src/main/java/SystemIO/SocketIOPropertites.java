package src.main.java.SystemIO;

import java.io.BufferedReader;//高效读取字节流
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;//网络地址和端口的封装类
import java.net.ServerSocket;//服务器端，Socket类
import java.net.Socket;//客户端Socket类

public class SocketIOPropertites {

    private static final int RECEIVE_BUFFER = 10;//服务器Socket接收缓冲区的大小
    private static final int SO_TIMEOUT = 0;//服务器Socket超时时间
    private static final boolean REUSE_ADDR = false;//是否允许地址复用
    private static final int BACK_LOG = 2;//服务器连接请求队列的最大长度
    private static final boolean CLI_KEEPALIVE = false;//客户端Socket是否启用保活机制
    private static final boolean CLI_OOB = false;//客户端是否接收紧急数据
    private static final int CLI_REC_BUF = 20;//客户端是否接收缓冲区大小
    private static final boolean CLI_REUSE_ADDR = false;//客户端是否允许地址复用
    private static final int CLI_SEND_BUF = 20;//客户端发送缓冲区大小
    //客户端是否启用延迟关闭
    private static final boolean CLI_LINGER = true;
    private static final int CLI_LINGER_N = 0;
    private static final int CLI_TIMEOUT = 0;//客户端超时时间
    private static final boolean CLI_NO_DELAY = false;//客户端是否禁用Nagle算法

    /*

    StandardSocketOptions.TCP_NODELAY
    StandardSocketOptions.SO_KEEPALIVE
    StandardSocketOptions.SO_LINGER
    StandardSocketOptions.SO_RCVBUF
    StandardSocketOptions.SO_SNDBUF
    StandardSocketOptions.SO_REUSEADDR

 */

    public static void main(String[] args) {
        ServerSocket server = null;//声明服务器Socket变量
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(9090), BACK_LOG);//绑定端口，设置连接队列长度
            //设置接收缓冲区、地址复用和超时时间
            server.setReceiveBufferSize(RECEIVE_BUFFER);
            server.setReuseAddress(REUSE_ADDR);
            server.setSoTimeout(SO_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server up use 9090!");
        try {
            while (true) {
                Socket client = server.accept();//阻塞等待客户端连接
                System.out.println("client port :" + client.getPort());
                client.setKeepAlive(CLI_KEEPALIVE);
                client.setOOBInline(CLI_OOB);
                client.setReceiveBufferSize(CLI_REC_BUF);
                client.setReuseAddress(CLI_REUSE_ADDR);
                client.setSendBufferSize(CLI_SEND_BUF);
                client.setSoLinger(CLI_LINGER, CLI_LINGER_N);
                client.setSoTimeout(CLI_TIMEOUT);
                client.setTcpNoDelay(CLI_NO_DELAY);
                //创建新线程处理客户端数据
                new Thread(
                        () -> {
                            try {
                                InputStream in = client.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                char[] data = new char[1024];//创建字节缓冲区
                                while (true) {
                                    int num = reader.read(data);//读取数据到缓冲区，返回读取的字符数
                                    if (num > 0) {
                                        System.out.println("client read some data is :" + num + " val :" + new String(data, 0, num));
                                    } else if (num == 0) {
                                        System.out.println("client readed nothing!");
                                        continue;
                                    } else {
                                        //断开客户端连接，关闭Socket并退出循环
                                        System.out.println("client readed - 1.....");
                                        System.in.read();
                                        client.close();
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                ).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
