package src.main.java.SystemIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;//服务器端套接字，用于监听客户端连接。
import java.net.Socket;//客户端套接字，代表一个与客户端的连接。
//基于BIO模型下的TCP服务器实现，用于接收客户端连接并处理数据
public class SocketIO {
    public static void main(String[] args) throws Exception {

        ServerSocket server = new ServerSocket(9090, 20);
        System.out.println("step1:new ServerSocket(9090)");
        //持续接收客户端的信息
        while (true) {
            //阻塞等待客户端连接，当有客户端连接时，返回一个Socket对象代表该连接
            Socket client = server.accept();
            System.out.println("step2:client\t" + client.getPort());
            //为每个客户端连接创建新线程处理数据
            new Thread(() -> {
                InputStream in = null;
                try {
                    in = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//按行来读取文本数据
                    while (true) {
                        String dataLine = reader.readLine();
                        if (null != dataLine) {
                            System.out.println(dataLine);
                        } else {
                            client.close();
                            break;
                        }
                    }
                    System.out.println("客户端断开");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

    }
}
