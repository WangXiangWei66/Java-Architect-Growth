package src.main.java.SystemIO;
//Java NIO单线程多路复用服务器代码
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SocketMultiplexingSingleThreadv1 {

    private ServerSocketChannel server = null;//服务器通道（用于监听客户端连接）
    private Selector selector = null;//多路复用选择器（监控多个通道的I/O事件
    int port = 9090;//服务器端口

    private void initServer() {
        try {
            server = ServerSocketChannel.open();//打开服务器通信
            server.configureBlocking(false);//设置为非阻塞模式
            server.bind(new InetSocketAddress(port));//绑定端口

            selector = Selector.open();//打开选择器，用于后续监听通道事件
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        initServer();//服务器初始化
        System.out.println("服务器启动了......");
        try {
            while (true) {
                Set<SelectionKey> keys = selector.keys();//获取所有已注册的键
                System.out.println(keys.size() + " size");//将已注册键的数量打印
                //阻塞等待事件发生，返回就绪的通道数量
                while (selector.select() > 0) {
                    //获取所有的就绪事件
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    //遍历每个就绪事件
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        //处理连接请求事件
                        if (key.isAcceptable()) {
                            acceptHandler(key);//连接事件
                        } else if (key.isReadable()) {
                            readHandler(key);//读事件
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptHandler(SelectionKey key) {
        try {
            //从SelectionKey中获取服务器通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();//接收客户端连接
            client.configureBlocking(false);//设置客户端通道为非阻塞
            ByteBuffer buffer = ByteBuffer.allocate(9192);//创建缓冲区，用于读写数据
            //将客户端通道注册到选择器，关注读事件，并附加缓冲区
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("--------------------");
            System.out.println("新客户端:" + client.getRemoteAddress());
            System.out.println("----------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readHandler(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();//获取客户端通道
        ByteBuffer buffer = (ByteBuffer) key.attachment();//获取附加缓冲区
        buffer.clear();
        int read = 0;
        try {
            while (true) {
                read = client.read(buffer);//从通道读取数据到缓冲区
                if (read > 0) {
                    buffer.flip();//切换缓冲区为读模式
                    //将获取到的数据写回客户端，回升服务器
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (read == 0) {
                    break;
                } else {
                    client.close();//客户端关闭了连接
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThreadv1 service = new SocketMultiplexingSingleThreadv1();
        service.start();
    }
}
