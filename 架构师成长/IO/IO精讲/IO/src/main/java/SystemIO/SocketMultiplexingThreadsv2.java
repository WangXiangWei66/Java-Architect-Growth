package src.main.java.SystemIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketMultiplexingThreadsv2 {


    public static void main(String[] args) throws IOException {
        //1个boss线程，负责接收连接
        EventLoopGroup boss = new EventLoopGroup(1);
        //3个worker线程，用来处理已连接的客户端连接
        EventLoopGroup worker = new EventLoopGroup(3);
        //创建服务器启动器
        ServerBootStrap b = new ServerBootStrap();
        b.group(boss, worker).bind(9090);
        System.in.read();//阻塞主线程，防止程序退出
    }
}
//服务器启动器
class ServerBootStrap {
    private EventLoopGroup group;//boss线程组
    private EventLoopGroup childedGroup;//worker线程组
    ServerAcceptr sAcceptr;//服务器Acceptor
    //设置boss和worker线程组
    public ServerBootStrap group(EventLoopGroup boss, EventLoopGroup worker) {
        group = boss;
        childedGroup = worker;
        return this;//支持链式调用
    }
    //绑定端口并启动服务器
    public void bind(int port) throws IOException {
        //打开服务器Socket通道
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);//设置为非阻塞
        server.bind(new InetSocketAddress(port));//绑定到指定端口
        //创建服务器用于接收连接
        sAcceptr = new ServerAcceptr(childedGroup, server);
        EventLoop eventLoop = group.chosser();//从boss线程组选择一个事件循环
        eventLoop.execute(new Runnable() {
            @Override
            public void run() {
                eventLoop.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //设置事件循环名称
                            eventLoop.name = Thread.currentThread() + eventLoop.name;
                            System.out.println("bind...server...to" + eventLoop.name);
                            //将服务器通道注册到选择器，关注Accept事件，并附加到acceptor
                            server.register(eventLoop.selector, SelectionKey.OP_ACCEPT, sAcceptr);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
//事件循环组
class EventLoopGroup {
    AtomicInteger cid = new AtomicInteger(0);//原子计数器，用于选择事件循环
    EventLoop[] childrens = null;//事件循环数组
    //构造方法，创建指定数量的事件循环
    EventLoopGroup(int nThreads) {
        childrens = new EventLoop[nThreads];
        for (int i = 0; i < nThreads; i++) {
            childrens[i] = new EventLoop("T" + i);
        }
    }
    //使用轮询的方式，选择下一个事件循环
    public EventLoop chosser() {
        return childrens[cid.getAndIncrement() % childrens.length];
    }
}

interface Handler {
    void doRead();
}
//客户端数据处理器
class ClientReader implements Handler {
    SocketChannel key;//客户端Socket通道
    //接收客户端的通信
    ClientReader(SocketChannel server) {
        this.key = server;
    }

    @Override
    public void doRead() {
        //创建缓冲区
        ByteBuffer data = ByteBuffer.allocate(4096);
        try {
            key.read(data);
            data.flip();
            //创建字节数组来接收数据
            byte[] dd = new byte[data.limit()];
            data.get(dd);
            System.out.println(new String(dd));
            data.clear();
            //向客户端发送字符
            for (int i = 0; i < 10; i++) {
                data.put("a".getBytes());
                data.flip();
                key.write(data);
                data.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//服务器连接接收器
class ServerAcceptr implements Handler {
    ServerSocketChannel key;//服务器Socket通道
    EventLoopGroup cGroup;//worker线程组

    ServerAcceptr(EventLoopGroup cGroup, ServerSocketChannel server) {
        this.key = server;
        this.cGroup = cGroup;
    }

    public void doRead() {
        try {
            final EventLoop eventLoop = cGroup.chosser();
            final SocketChannel client = key.accept();
            client.configureBlocking(false);
            client.setOption(StandardSocketOptions.TCP_NODELAY, true);//目的是为了提高实时性
            //创建客户端处理器
            final ClientReader cHandler = new ClientReader(client);
            //向选中的事件循环提交任务
            eventLoop.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        System.out.println("socket...send...to " + eventLoop.name + " client port : " + client.socket().getPort());

                        client.register(eventLoop.selector, SelectionKey.OP_READ, cHandler);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
//事件循环
class EventLoop implements Executor {
    Selector selector;
    Thread thread = null;//事件循环运行的线程
    BlockingQueue events = new LinkedBlockingQueue();
    int NOT_STARTED = 1;
    int STARTED = 2;
    AtomicInteger STAT = new AtomicInteger(1);//状态原子变量
    String name;

    public EventLoop(String name) {
        try {
            this.name = name;
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws InterruptedException, IOException {
        System.out.println("server已经开始：");
        for (; ; ) {
            //阻塞等地啊就绪事件
            int nums = selector.select();
            if (nums > 0) {//有就绪事件
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    //获得附加处理器
                    Handler handler = (Handler) key.attachment();
                    if (handler instanceof ServerAcceptr) {
                    } else if (handler instanceof ClientReader) {
                    }
                    handler.doRead();
                }
            }
            runrTask();//运行任务队列中的任务
        }
    }

    @Override
    public void execute(Runnable task) {
        try {
            events.put(task);
            this.selector.wakeup();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //如果不在事件循环中，且事件是未启动的
        if (!inEventLoop() && STAT.incrementAndGet() == STARTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        thread = Thread.currentThread();
                        EventLoop.this.run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    //运行任务队列中的任务
    public void runrTask() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Runnable task = (Runnable) events.poll(10, TimeUnit.MICROSECONDS);
            if (task != null) {
                events.remove(task);
                task.run();
            }
        }
    }
    //判断当线程是否为事件循环线程
    private boolean inEventLoop() {
        return thread == Thread.currentThread();
    }
}