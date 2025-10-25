package src.main.java.SystemIO.TestRactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {
    //IO多路复用器
    Selector selector = null;
    //待注册通道队列
    LinkedBlockingQueue<Channel> lbq = get();
    //关联的Selector线程组
    SelectorThreadGroup stg;

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        return new LinkedBlockingQueue<>();
    }

    SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;//绑定线程组
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int nums = selector.select();//阻塞，直到有通道触发注册的事件
                if (nums > 0) {
                    // 2.1 获取所有触发事件的SelectionKey（每个Key对应一个通道+事件的绑定关系）
                    Set<SelectionKey> keys = selector.selectedKeys();
                    // 2.2 遍历处理每个SelectionKey（必须迭代器移除，避免重复处理）
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        // 2.3 分类型处理事件：ACCEPT（接受连接）、READ（读取数据）、WRITE（写入数据）
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }
                //处理待注册通道队列，将队列 中的通道注册到当前Selector
                if (!lbq.isEmpty()) {
                    Channel c = lbq.take();//阻塞获取队列中的通道
                    //如果时服务器监听通道，注册ACCEPT事件
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + "register listen");
                        //如果是服务器连接通道，注册Read事件，并绑定ByteBuffer作为附件
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println(Thread.currentThread().getName() + "register client:" + client.getRemoteAddress());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "read....");
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            try {
                int num = client.read(buffer);
                if (num > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                } else if (num == 0) {
                    break;
                } else if (num < 0) {
                    System.out.println("client:" + client.getRemoteAddress() + "closed.....");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println(Thread.currentThread().getName() + "  acceptHandler.....");
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            stg.nextSelectorV3(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //线程组绑定
    public void setWorker(SelectorThreadGroup stgWorker) {
        this.stg = stgWorker;
    }
}