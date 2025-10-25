package src.main.java.SystemIO.TestRactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorThreadGroup {

    SelectorThread[] sts;
    ServerSocketChannel server = null;
    //实现线程安全的轮询索引
    AtomicInteger xid = new AtomicInteger(0);
    SelectorThreadGroup stg = this;

    public void setWorker(SelectorThreadGroup stg) {
        this.stg = stg;
    }
    //初始化线程
    SelectorThreadGroup(int num) {
        sts = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            sts[i] = new SelectorThread(this);//形成线程组
            new Thread(sts[i]).start();//启动所有的线程
        }
    }
    //绑定端口
    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            nextSelectorV3(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //通道分配方法
    public void nextSelectorV3(Channel c) {
        try {
            //服务端监听通道
            if (c instanceof ServerSocketChannel) {
                SelectorThread st = next();
                st.lbq.put(c);//将通道放入线程的待注册队列
                st.setWorker(stg);//设置线程的关联线程组
                st.selector.wakeup();//唤醒线程组，使其立即处理新通道
                //客户端连接通道
            } else {
                SelectorThread st = nextV3();
                st.lbq.add(c);
                st.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV2(Channel c) {
        try {
            if (c instanceof ServerSocketChannel) {
                sts[0].lbq.put(c);
                sts[0].selector.wakeup();
            } else {
                SelectorThread st = nextV2();
                st.lbq.add(c);
                st.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextSelector(Channel c) {
        SelectorThread st = next();
        st.lbq.add(c);
        st.selector.wakeup();
    }

    private SelectorThread next() {
        int index = xid.incrementAndGet() % sts.length;//原子操作保证线程安全
        return sts[index];
    }

    private SelectorThread nextV2() {
        int index = xid.incrementAndGet() % (sts.length - 1);
        return sts[index + 1];
    }

    private SelectorThread nextV3() {
        int index = xid.incrementAndGet() % sts.length;
        return sts[index + 1];
    }
}
