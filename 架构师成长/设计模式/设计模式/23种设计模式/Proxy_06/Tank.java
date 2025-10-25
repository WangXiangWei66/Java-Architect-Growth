package Proxy_06;

import java.util.Random;

//本方法开始使用代理:通过组合而不是继承的方式来实现功能增强
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

    public static void main(String[] args) {
        //下面的两个代理方法只是声明了，但是没有初始化
        new TankTimeProxy().move();
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}

class TankTimeProxy implements Movable {
    Tank tank;//变成了组合关系

    @Override
    public void move() {
        long start = System.currentTimeMillis();
        tank.move();//此处会抛空指针异常
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}

class TankLogProxy implements Movable {
    Tank tank;

    @Override
    public void move() {
        System.out.println("start moving....");
        tank.move();//会抛空指针异常
        System.out.println("stopped!");
    }
}

