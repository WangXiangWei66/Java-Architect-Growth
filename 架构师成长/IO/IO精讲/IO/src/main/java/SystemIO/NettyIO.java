package src.main.java.SystemIO;

import io.netty.bootstrap.ServerBootstrap;//服务器端引导类，用于配置和启动服务器
import io.netty.buffer.ByteBuf;//Netty的缓冲区类，用于高效处理字节数据
import io.netty.channel.ChannelHandlerContext; //通道处理器上下文，用于操作通道和传递数据
import io.netty.channel.ChannelInboundHandlerAdapter;//入站处理器适配器，用于处理入站事件（如接收数据）
import io.netty.channel.ChannelInitializer;//通道初始化器，用于初始化新连接的通道
import io.netty.channel.ChannelOption;//通道选项常量，用于配置通道参数
import io.netty.channel.ChannelPipeline;//通道流水线，存储一系列处理器（Handler）
import io.netty.channel.nio.NioEventLoopGroup;//基于NIO的事件循环组，管理处理I/O的线程
import io.netty.channel.socket.nio.NioServerSocketChannel;//基于NIO的服务器套接字通道（监听连接）
import io.netty.channel.socket.nio.NioSocketChannel;//基于NIO的客户端套接字通道（处理连接）

//基于Netty框架实现的TCP服务器程序，用于接受客户端连接并处理数据
public class NettyIO {

    public static void main(String[] args) {
        //创建事件的循环组
        NioEventLoopGroup boss = new NioEventLoopGroup(2);//boss负责接收客户端的连接
        NioEventLoopGroup worker = new NioEventLoopGroup(2);//worker负责处理连接的I/O事件
        //创建服务器引导类，用于配置服务器
        ServerBootstrap boot = new ServerBootstrap();
        try {
            //用于配置服务器参数
            boot.group(boss, worker)//设置线程组
                    .channel(NioServerSocketChannel.class)//指定服务器的通道类型
                    .option(ChannelOption.TCP_NODELAY, false)//配置服务器的通道选项
                    //子通道初始化器
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();//获取通道流水线
                            //向流水线中增加5个自定义入站处理器
                            p.addLast(new MyInbound());
                            p.addLast(new MyInbound());
                            p.addLast(new MyInbound());
                            p.addLast(new MyInbound());
                            p.addLast(new MyInbound());
                        }
                    })
                    .bind(9999)
                    .sync()
                    //获取服务器通信
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MyInbound extends ChannelInboundHandlerAdapter {
    //通道读取到事件时，会触发这个
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int size = buf.writerIndex();
        byte[] data = new byte[size];
        //将缓冲区数据复制到字节数组
        buf.getBytes(0, data);
        String dd = new String(data);
        String[] strs = dd.split("\n");
        for (String str : strs) {
            System.out.println("触发的命令：" + str + "...");
        }
        ctx.write(msg);//将消息写回通道
    }
    //当通道读取数据完成时会触发
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    //通道取消注册时会触发
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端断开了连接");
        super.channelUnregistered(ctx);//保证可以正常传播
    }
}