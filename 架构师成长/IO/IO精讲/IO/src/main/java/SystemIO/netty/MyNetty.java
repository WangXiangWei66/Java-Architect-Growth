package src.main.java.SystemIO.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;//管理多个线程处理IO操作
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;
import io.netty.buffer.*;

import java.net.InetSocketAddress;


public class MyNetty {

    @Test
    public void myBytebuf() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
    }

    public static void print(ByteBuf buf) {
        System.out.println("buf.isReadable()    " + buf.isReadable());//是否有可读的字节数据
        System.out.println("buf.readerIndex()   " + buf.readerIndex());//打印读索引
        System.out.println("buf.readableBytes() " + buf.readableBytes());//打印可读字节数
        System.out.println("buf.isWritable()    " + buf.isWritable());//是否还可写
        System.out.println("buf.writerIndex()   " + buf.writerIndex());//下一个写索引的位置
        System.out.println("buf.writableBytes() " + buf.writableBytes());//可写字节数
        System.out.println("buf.capacity()      " + buf.capacity());//当前容量
        System.out.println("buf.maxCapacity()   " + buf.maxCapacity());//最大容量
        System.out.println("buf.isDirect()      " + buf.isDirect());//是否为直接缓冲区
        System.out.println("---------------");
    }

    @Test
    public void loopExecutor() throws Exception {
        NioEventLoopGroup selector = new NioEventLoopGroup(2);
        selector.execute(() -> {
            try {
                for (; ; ) {
                    System.out.println("hello world001");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        selector.execute(() -> {
            try {
                for (; ; ) {
                    System.out.println("hello world002");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.in.read();
    }

    //初始化组件 → 建立连接 → 发送 / 接收数据 → 处理事件 → 关闭资源
    @Test
    public void clientMode() throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        thread.register(client);
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());
        ChannelFuture connect = client.connect(new InetSocketAddress("192.168.150.11", 9090));
        ChannelFuture sync = connect.sync();
        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(buf);
        send.sync();
        sync.channel().closeFuture().sync();
        System.out.println("client over....");
    }

    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connect = bs.group(group)
                .channel(NioSocketChannel.class)//指定客户端通道类型
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();//获取通道的处理器通道
                        p.addLast(new MyInHandler());//向管道中添加自定义入站处理器
                    }
                })
                .connect(new InetSocketAddress("192.168.150.11", 9090));
        Channel client = connect.sync().channel();
        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        ChannelFuture send = client.writeAndFlush(buf);
        send.sync();
        client.closeFuture().sync();
    }

    @Test
    public void serverMode() throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();//服务器监听通道
        thread.register(server);
        ChannelPipeline p = server.pipeline();
        p.addLast(new MyAcceptHandler(thread, new ChannelInit()));
        ChannelFuture bind = server.bind(new InetSocketAddress("192.168.150.11", 9090));
        // 阻塞等待绑定完成 → 获取服务器通道 → 阻塞等待通道关闭（保证服务器不退出）
        bind.sync().channel().closeFuture().sync();
        System.out.println("server close...");
    }
}

//处理客户端连接
class MyAcceptHandler extends ChannelInboundHandlerAdapter {
    private final EventLoopGroup selector;//注册客户端通道的事件循环组

    private final ChannelHandler handler;//客户端通道的初始化器

    public MyAcceptHandler(EventLoopGroup selector, ChannelHandler handler) {
        this.selector = selector;
        this.handler = handler;
    }

    //服务器通道注册到事件循环组时触发
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server registerd...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel client = (SocketChannel) msg;//将接收到的连接请求转化为客户端通道
        ChannelPipeline p = client.pipeline();
        p.addLast(handler);
        selector.register(client);
    }
}

//通道初始化器
@ChannelHandler.Sharable
class ChannelInit extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());
        ctx.pipeline().remove(this);
    }
}

//自定义连接处理器
class MyInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client registed....");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client active...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
        System.out.println(str);
        ctx.writeAndFlush(buf);
    }
}

