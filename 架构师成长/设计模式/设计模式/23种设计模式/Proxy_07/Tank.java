package Proxy_07;

import java.util.Random;
//通过代理链的方式实现了多个代理功能的组合
public class Tank implements Movable {
    @Override
    public void move() {
        System.out.println("Tank moving claclacla.....");
        try {
            Thread.sleep(new Random().nextInt(100000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //日志记录→时间统计→核心移动→时间统计→日志记录
    public static void main(String[] args) {
        Tank t = new Tank();//创建被代理对象
        TankTimeProxy ttp = new TankTimeProxy(t);//创建时间代理
        TankLogProxy tlp = new TankLogProxy(ttp);
        tlp.move();
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}

class TankTimeProxy implements Movable {
    Movable m;//改为了依赖接口（这是面向接口编程）

    public TankTimeProxy(Movable m) {
        this.m = m;
    }

    @Override
    public void move() {
        long start = System.currentTimeMillis();
        m.move();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}

class TankLogProxy implements Movable {
    Movable m;

    public TankLogProxy(Movable m) {
        this.m = m;
    }

    @Override
    public void move() {
        System.out.println("start moving....");
        m.move();//此处调用的是TankTimeProxy方法
        System.out.println("stopped!");
    }
}

