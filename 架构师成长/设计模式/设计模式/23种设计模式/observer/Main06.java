package observer;
//有很多时候，观察者需要根据事件的具体情况来进行处理

import java.util.ArrayList;
import java.util.List;

class Child06 {
    private boolean cry = false;
    private List<Observer06> observers = new ArrayList<>();

    {
        observers.add(new Dad06());
        observers.add(new Mum06());
        observers.add(new Dog06());
    }

    public boolean isCry() {
        return cry;
    }

    public void wakeUp() {
        cry = true;
        //创建事件对象，封装事件和位置信息
        wakeUpEvent event = new wakeUpEvent(System.currentTimeMillis(), "bed");
        for (Observer06 o : observers) {
            o.actionOnWakeUp(event);
        }
    }
}

interface Observer06 {
    void actionOnWakeUp(wakeUpEvent event);
}

class wakeUpEvent {
    long timeStamp;//事件发生的时间戳
    String loc;//事件发生的位置

    public wakeUpEvent(long timeStamp, String loc) {
        this.timeStamp = timeStamp;
        this.loc = loc;
    }
}

class Dad06 implements Observer06 {
    public void feed() {
        System.out.println("dad feeding...");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        feed();
    }
}

class Mum06 implements Observer06 {

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        hug();
    }

    public void hug() {
        System.out.println("Mum hugging.......");
    }
}

class Dog06 implements Observer06 {

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        wang();
    }

    public void wang() {
        System.out.println("dog wang.......");
    }
}

public class Main06 {
    public static void main(String[] args) {
        Child06 c = new Child06();
        c.wakeUp();
    }
}
