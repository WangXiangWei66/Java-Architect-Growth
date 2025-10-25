package src.main.java.SystemIO;
//本代码基于java NIO的多线程Socket服务器，采用了Reactor模式的变体，使用多个Selector来实现I/O多路复用
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketMultiplexingThreads {

    private ServerSocketChannel server = null;
    private Selector selector1 = null;
    private Selector selector2 = null;
    private Selector selector3 = null;
    int port = 9090;

    public void initServer() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);//设置为非阻塞
            server.bind(new InetSocketAddress(port));
            selector1 = Selector.open();
            selector2 = Selector.open();
            selector3 = Selector.open();
            server.register(selector1, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class NioThread extends Thread {
        Selector selector = null;//当前线程负责的Selector
        static int selectors = 0;//工作Selector数量
        int id = 0;//当前工作线程id
        volatile static BlockingQueue<SocketChannel>[] queue;//阻塞队列数组，用于 Boss 线程向 Worker 线程传递 SocketChannel
        static AtomicInteger idx = new AtomicInteger();//轮询非陪客户端到不同的Worker
        //创建主线程
        NioThread(Selector sel, int n) {
            this.selector = sel;
            this.selectors = n;

            queue = new LinkedBlockingQueue[selectors];
            for (int i = 0; i < n; i++) {
                queue[i] = new LinkedBlockingQueue<>();
            }
            System.out.println("Boss 启动");
        }

        NioThread(Selector sel) {
            this.selector = sel;
            id = idx.getAndIncrement() % selectors;
            System.out.println("worker :" + id + "启动");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    //轮询Selector获取就绪事件
                    while (selector.select(10) > 0) {
                        Set<SelectionKey> selectionKeys = selector.selectedKeys();
                        Iterator<SelectionKey> iter = selectionKeys.iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = iter.next();
                            iter.remove();
                            if (key.isAcceptable()) {
                                acceptHandler(key);
                            } else if (key.isReadable()) {
                                readHander(key);
                            }
                        }
                    }
                    //检查是否有新的客户端需要注册
                    if (!queue[id].isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.allocate(8192);
                        SocketChannel client = queue[id].take();
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println("------------------------------------");
                        System.out.println("新客户端：" + client.socket().getPort() + "分配到：" + (id));
                        System.out.println("------------------------------------");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void acceptHandler(SelectionKey key) {
            try {
                //获取服务器通道，并接收客户端连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel client = ssc.accept();
                client.configureBlocking(false);
                //轮询选择一个Worker线程
                int num = idx.getAndIncrement() % selectors;
                queue[num].add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void readHander(SelectionKey key) {
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();//获取附加缓冲区
            buffer.clear();
            int read = 0;
            try {
                while (true) {
                    read = client.read(buffer);
                    if (read > 0) {
                        buffer.flip();//切换为读模式
                        //将读取的数据写回客户端
                        while (buffer.hasRemaining()) {
                            client.write(buffer);
                        }
                        buffer.clear();
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
    }

    public void main(String[] args) {
        SocketMultiplexingThreads service = new SocketMultiplexingThreads();
        service.initServer();
        //Boss线程负责处理两个连接请求
        NioThread T1 = new NioThread(service.selector1, 2);
        NioThread T2 = new NioThread(service.selector2);
        NioThread T3 = new NioThread(service.selector3);

        T1.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T2.start();
        T3.start();
        System.out.println("服务器启动了......");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
