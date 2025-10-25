package src.main.java.SystemIO;
//单线程NIO的多路复用器
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SocketMultiplexingSingleThreadv1_1 {
    //服务器Socket通道，用于监听客户端连接（TCP协议）
    private ServerSocketChannel server = null;
    //选择器：NIO的核心组件，用于"多路复用"监听多个通道的事件（如连接、读、写）
    private Selector selector = null;
    int port = 9090;//服务器监听端口
    //服务器初始化
    public void initServer() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);//设置通道为非阻塞
            server.bind(new InetSocketAddress(port));
            selector = Selector.open();//打开选择器来监听通道事件
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //服务器主循环
    public void start() {
        initServer();
        System.out.println("服务器启动了......");
        try {
            while (true) {
                //选择器阻塞等待事件
                while (selector.select() > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    //迭代处理每个就绪事件
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        //根据事件类型分发到对应处理器
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {
                            writeHandler(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //处理客户端连接事件
    private void acceptHandler(SelectionKey key) {
        try {
            //获取服务器通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();
            client.configureBlocking(false);
            //分配缓冲区来存储客户端发送的数据
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("----------------------------------");
            System.out.println("新客户端：" + client.getRemoteAddress());
            System.out.println("----------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //处理客户端读取事件
    private void writeHandler(SelectionKey key) {
        System.out.println("write handler....");
        //获取客户端通道
        SocketChannel client = (SocketChannel) key.channel();
        //获取之前附加的缓冲区
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        //切换缓冲区为读模式
        buffer.flip();
        //循环向客户端写数据，直到缓冲区中不再有剩余数据
        while (buffer.hasRemaining()) {
            try {
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //模拟业务处理的延迟
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buffer.clear();
        key.cancel();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readHandler(SelectionKey key) {
        System.out.println("read handler.....");
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        //清空缓冲区，并切换为写模式
        buffer.clear();
        int read = 0;
        try {
            while (true) {
                read = client.read(buffer);
                if (read > 0) {
                    client.register(key.selector(), SelectionKey.OP_WRITE, buffer);
                } else if (read == 0) {
                    break;
                } else {
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThreadv1_1 service = new SocketMultiplexingSingleThreadv1_1();
        service.start();
    }
}
