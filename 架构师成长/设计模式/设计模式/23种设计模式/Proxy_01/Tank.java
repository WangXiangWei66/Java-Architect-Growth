package Proxy_01;

import java.util.Random;

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
}
//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}
