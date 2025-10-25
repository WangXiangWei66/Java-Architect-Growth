package Proxy_05;

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
        new TankTimeProxy(new Tank()).move();
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}

class TankTimeProxy implements Movable {

    Tank tank;//变成了组合关系

    public TankTimeProxy(Tank tank) {
        this.tank = tank;
    }

    @Override
    public void move() {
        long start = System.currentTimeMillis();
        tank.move();//调用父类的核心移动逻辑
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}

