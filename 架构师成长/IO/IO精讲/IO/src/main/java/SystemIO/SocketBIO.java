package src.main.java.SystemIO;
//捕获IO操作可能出现的异常
import java.io.BufferedReader;
import java.io.IOException;
//处理输入流，读取客户端发送的数据
import java.io.InputStream;
import java.io.InputStreamReader;
//实现TCP协议的服务器端和客户端套接字
import java.net.ServerSocket;
import java.net.Socket;
//基于BIO模型的TCP服务器实现，用于接收客户端的连接并处理数据
public class SocketBIO {

    public static void main(String[] args) throws Exception {
        //创建了服务器套接字，绑定了端口，允许的最大等待队列的长度为20
        ServerSocket server = new ServerSocket(9090, 20);
        System.out.println("step1:new ServerSocket(9090)");
        //无限循环，持续接收客户端连接
        while (true) {
            Socket client = server.accept();//阻塞等大客户端连接
            System.out.println("step2:client\t" + client.getPort());
            //为每一个客户端连接创建一个新线程来处理数据
            new Thread(new Runnable() {
                public void run() {
                    InputStream in = null;//声明输入流，用于读取客户端数据
                    try {
                        in = client.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        while (true) {
                            //阻塞读取另一行数据，BIO的另一个阻塞点
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
                }
            }).start();//启动线程来处理当前的客户端
        }

    }
}
