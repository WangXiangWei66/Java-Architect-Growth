package Proxy_08;
//引入了JDK动态代理
import java.lang.reflect.InvocationHandler;//动态代理的 "调用处理器" 接口，负责定义代理逻辑（增强功能）
import java.lang.reflect.Method;//反射中用于表示方法的类，可通过它调用被代理对象的方法
import java.lang.reflect.Proxy;
import java.util.Random;

public class Tank implements Movable {
    @Override
    //move符合单一职责原则
    public void move() {
        System.out.println("Tank moving claclacla.....");
        try {
            Thread.sleep(new Random().nextInt(100000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Tank tank = new Tank();
        Movable m = (Movable) Proxy.newProxyInstance(Tank.class.getClassLoader(),
                new Class[]{Movable.class},
                new LogHander(tank)
        );
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}

class LogHander implements InvocationHandler {
    Tank tank;

    public LogHander(Tank tank) {
        this.tank = tank;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("method" + method.getName() + "start...");
        Object o = method.invoke(tank, args);
        System.out.println("method" + method.getName() + "end!");
        //返回真实的返回值
        return o;
    }
}
