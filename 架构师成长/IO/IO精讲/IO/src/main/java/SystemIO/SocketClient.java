package src.main.java.SystemIO;

import java.io.BufferedReader;//包装输入流，方便按行读取用户输入
import java.io.IOException;//捕获网络连接和IO操作的异常
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;//实现TCP客户端套接字，用于服务器建立连接
//基于BIO的TCP客户端实现，用于连接指定服务器并发送数据
public class SocketClient {

    public static void main(String[] args) {
        try {
            Socket client = new Socket("192.168.150.11", 9090);
            client.setSendBufferSize(20);//设置缓冲区的大小
            client.setTcpNoDelay(true);//关闭Nagle算法，立即发送小数据包，不延迟
            OutputStream out = client.getOutputStream();//获取输出流，用于向服务器发送数据
            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            //循环读取用户输入，并发送给服务器
            while (true) {
                //阻塞读取用户输入的一行文本
                String line = reader.readLine();
                if (line != null) {
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}