package cglib;

import net.sf.cglib.proxy.Enhancer;//创建代理类
import net.sf.cglib.proxy.MethodInterceptor;//方法拦截器接口
import net.sf.cglib.proxy.MethodProxy;//方法代理类

import java.lang.reflect.Method;//反射包的method类
import java.util.Random;

/*
CGLIB实现动态代理不需要接口，通过继承方式实现代理
 */
public class Main {

    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();//生成代理类
        enhancer.setSuperclass(Tank.class);//设置代理的目标类
        enhancer.setCallback(new TimeMethodInterceptor());//设置回溯拦截器
        Tank tank = (Tank) enhancer.create();//创建代理对象
        tank.move();
    }
}

class TimeMethodInterceptor implements MethodInterceptor {

    @Override
    /*
    o：代理对象本身
    method:被调用的目标方法
    objects:方法的参数列表
    methodProxy:方法代理对象，调用目标方法
     */
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println(o.getClass().getSuperclass().getName());//打印代理对象的父类名称
        System.out.println("before");
        Object result = null;
        result = methodProxy.invokeSuper(o, objects);
        System.out.println("after");
        return result;
    }
}

class Tank {
    public void move() {
        System.out.println("Tank moving claclacla....");
        try {
            Thread.sleep(new Random().nextInt(10000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
