package src.main.java.SystemIO;

import io.netty.bootstrap.Bootstrap;//引导客户端，通过他可以配置和启动客户端连接，设置线程组、通道类型、远程地址等参数
import io.netty.buffer.ByteBuf;//Netty缓冲区接口，用于存储和操作数据
import io.netty.buffer.Unpooled;//创建非池化ByteBuf的静态方法，非池化缓冲区在使用后会被垃圾回收，适用于一些内存使用量不大且对内存分配和回收不太敏感的场景
import io.netty.channel.Channel;//I/O操作的基本抽象，代表一个网络连接或文件等I/O资源，提供了读写、关闭等操作方法
import io.netty.channel.ChannelFuture;//用于异步操作的结果表示，通过他可以监听操作的完成状态、获取操作结果或处理操作过程中的异常
import io.netty.channel.ChannelInitializer;//初始化通道类，在通道被注册到时间循环时会被调用，通常在其中配置通道的流水线，添加各种处理器
import io.netty.channel.ChannelPipeline;//通道处理器链，包含一系列的ChannelHandler，用于处理入站和出战事件，按照添加的顺序依次执行
import io.netty.channel.nio.NioEventLoopGroup;//基于 NIO（非阻塞 I/O）的事件循环组，用于管理处理 I/O 操作的线程，负责处理注册到它上面的通道的 I/O 事件。
import io.netty.channel.socket.SocketChannel;//套接字通道的接口，用于 TCP 网络通信。
import io.netty.channel.socket.nio.NioSocketChannel;//基于 NIO 的套接字通道实现类，用于在非阻塞模式下进行 TCP 网络通信。
//使用Netty框架编写的Java客户端代码，用于与指定的服务器建立连接并发送数据
public class NettyClient {

    public static void main(String[] args) {
        try {
            NioEventLoopGroup worker = new NioEventLoopGroup();//用于处理I/O操作的线程池
            Bootstrap boot = new Bootstrap();//引导客户端的连接
            //设置NioEventLoopGroup
            boot.group(worker)
                    .channel(NioSocketChannel.class)//指定通道类型
                    .remoteAddress("192.168.150.11", 9090)//设置服务器的远程地址和端口
                    //设置通道初始化器，用于配置通道的流水线
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println("初始化client");
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new MyInbound());//添加自定义的入站处理器到通道流水线
                        }
                    });
            //异步连接到服务器，sync()会阻塞直到连接完成
            ChannelFuture conn = boot.connect().sync();
            Channel client = conn.channel();//获取连接成功后的通道
            System.out.println(client);
            //创建缓冲区
            ByteBuf byteBuf = Unpooled.copiedBuffer("hello world".getBytes());
            //将缓冲区的数据写入通道并刷新
            client.writeAndFlush(byteBuf).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}