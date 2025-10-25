package observer_v7;
//处理对象之间的一对多依赖关系
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
class wakeUpEvent {
    long timestamp;//时间
    String loc;//地点
    Child source;//来源

    public wakeUpEvent(long timestamp, Child source, String loc) {
        this.timestamp = timestamp;
        this.source = source;
        this.loc = loc;
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