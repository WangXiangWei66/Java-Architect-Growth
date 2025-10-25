package Proxy_10;
//通过反射观察生成的代理对象

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;//处理反射获取的方法数组
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
        //通过系统属性设置，让JDK动态代理生成的代理类字节码保存到本地文件中，方便查看代理类的结构
        System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");
        Movable m = (Movable) Proxy.newProxyInstance(Tank.class.getClassLoader(),
                new Class[]{Movable.class},
                new TimeProxy(tank)
        );
        m.move();
    }
}

//任何实现Movable接口的类都必须提供move方法的具体实现
interface Movable {
    void move();
}

//定义实现统计的增强逻辑
class TimeProxy implements InvocationHandler {
    Movable m;

    public TimeProxy(Movable m) {
        this.m = m;
    }

    public void before() {
        System.out.println("method start...");
    }

    public void after() {
        System.out.println("method stop...");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Arrays.stream(proxy.getClass().getMethods()).map(Method::getName).forEach(System.out::println);
        before();
        Object o = method.invoke(m, args);
        after();
        //返回真实的返回值
        return o;
    }
}
