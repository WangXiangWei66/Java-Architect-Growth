package src.main.java.SystemIO;
/*
基于Java NIO 实现的单线程多路复用服务器
*/
import java.io.IOException;
import java.net.InetSocketAddress;//表示IP地址和端口的组合
import java.nio.ByteBuffer;//NIO核心缓冲区，用于数据存储和传输
import java.nio.channels.SelectionKey;//事件注册的钥匙，关联通道、选择器和事件类型
import java.nio.channels.Selector;//NIO多路复用核心，监听多个通道的就绪事件
import java.nio.channels.ServerSocketChannel;//服务器端TCP通信，用于监听客户端连接
import java.nio.channels.SocketChannel;//客户端TCP通信，用于与客户端通信
import java.util.Iterator;//基于迭代处理选择器的就绪事件集合
import java.util.Set;//存储选择器的就绪事件集合

public class SocketMultiplexingSingleThreadv2 {

    //服务器Socket通道，用于监听客户端连接（TCP协议）
    private ServerSocketChannel server = null;
    //选择器：NIO的核心组件，用于"多路复用"监听多个通道的事件（如连接、读、写）
    private Selector selector = null;
    int port = 9090;//服务器监听端口

    //服务器初始化，并完成首次事件注册，是服务器启动的基础
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

    //服务器主循环，负责启动初始化、阻塞等待事件、迭代处理就绪事件
    public void start() {
        initServer();
        System.out.println("服务器启动了......");
        try {
            while (true) {
                //选择器阻塞等待事件，最多阻塞50ms
                while (selector.select(50) > 0) {
                    //获取所有就绪事件的集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    //迭代处理每个就绪事件
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();//关联通道、选择器、事件类型
                        iter.remove();
                        //根据事件类型分发到对应处理器
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            System.out.println("in......");
                            //暂时取消读事件监听
                            key.interestOps(key.interestOps() | ~SelectionKey.OP_READ);
                            readHandler(key);
                        } else if (key.isWritable()) {
                            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                            writeHandler(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理客户端连接事件，接收连接、创建客户端通道、注册读事件
    private void acceptHandler(SelectionKey key) {
        try {
            //获取服务器通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();
            client.configureBlocking(false);
            //分配缓冲区来存储客户端发送的数据
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            client.register(selector, SelectionKey.OP_READ, buffer);//附加缓冲区是为了后续读事件处理时，可直接从SelectionKey中获取缓冲区，避免额外存储
            System.out.println("----------------------------------");
            System.out.println("新客户端：" + client.getRemoteAddress());
            System.out.println("----------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理客户端写事件，向客户端通道写入数据（用新线程处理，避免阻塞主线程）
    //主选择器线程仅负责事件调度，业务处理交给子线程
    private void writeHandler(SelectionKey key) {
        new Thread(() -> {
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
                    client.write(buffer);//返回实际写入的字节数
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
        }).start();
    }

    public void readHandler(SelectionKey key) {
        new Thread(() -> {
            System.out.println("read handler.....");
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            //清空缓冲区，并切换为写模式
            buffer.clear();
            int read = 0;//记录每次读取的字节数
            try {
                while (true) {
                    read = client.read(buffer);
                    System.out.println(Thread.currentThread().getName() + " " + read);
                    if (read > 0) {
                        key.interestOps(SelectionKey.OP_READ);
                        client.register(key.selector(), SelectionKey.OP_WRITE, buffer);//注册写事件到选择器
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
        }).start();

    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThreadv2 service = new SocketMultiplexingSingleThreadv2();
        service.start();
    }
}
