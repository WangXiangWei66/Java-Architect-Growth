package Proxy_02;

import java.util.Random;

public class Tank implements Movable {
    @Override
    public void move() {

        long start = System.currentTimeMillis();
        System.out.println("Tank moving claclacla.....");
        try {
            Thread.sleep(new Random().nextInt(100000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println((end - start));
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}
