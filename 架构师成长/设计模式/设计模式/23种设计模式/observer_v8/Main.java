package observer_v8;
/**
 * 有很多时候，观察者需要根据事件的具体情况来进行处理
 * 大多数时候，我们处理事件的时候，需要事件源对象
 * 事件也可以形成继承体系
 */

import state_thread.Action;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Child c = new Child();
        c.wakeUp();
    }
}

//定义观察者的统一接口，包含接收通知的方法
interface Observer {
    void actionOnWakeUp(wakeUpEvent event);
}

//封装通知的详细信息
class wakeUpEvent extends Event<Child> {
    long timestamp;//时间
    String loc;//地点
    Child source;//来源

    public wakeUpEvent(long timestamp, Child source, String loc) {
        this.timestamp = timestamp;
        this.source = source;
        this.loc = loc;
    }
    //实现抽象方法，返回事件源
    @Override
    Child getSource() {
        return source;
    }
}

//被观察者，并通知所有的观察者
class Child {
    private boolean cry = false;
    private List<Observer> observers = new ArrayList<>();//维护观察者的列表

    {
        observers.add(new Dad());
        observers.add(new Mum());
        observers.add(new Dog());
        //匿名观察者，通过lambda表达式实现Observer接口
        observers.add((e) -> {
            System.out.println("ppp");
        });
    }

    public boolean isCry() {
        return cry;
    }

    public void wakeUp() {
        cry = true;
        //创建事件对象，封装事件详情
        wakeUpEvent event = new wakeUpEvent(System.currentTimeMillis(), this, "bed");
        for (Observer o : observers) {
            o.actionOnWakeUp(event);
        }
    }
}
//事件抽象与具体实现
abstract class Event<T> {
    abstract T getSource();
}


class Dad implements Observer {

    public void feed() {
        System.out.println("dad feeding....");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        feed();
    }
}

class Mum implements Observer {

    public void hug() {
        System.out.println("mum hugging...");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        hug();
    }
}

class Dog implements Observer {

    public void wang() {
        System.out.println("dog wang....");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        wang();
    }
}