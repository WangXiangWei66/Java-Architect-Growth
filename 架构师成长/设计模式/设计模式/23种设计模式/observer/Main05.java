package observer;

import java.util.ArrayList;
import java.util.List;


//分离观察者和被观察者
public class Main05 {

    public static void main(String[] args) {
        Child05 c = new Child05();
        c.wakeUp();
    }
}

class Child05 {
    private boolean cry = false;
    private List<Observer> observers = new ArrayList<>();

    {
        observers.add(new Dad05());
        observers.add(new Mum05());
        observers.add(new Dog05());
    }

    public boolean isCry() {
        return cry;
    }

    public void wakeUp() {
        cry = true;
        wakeUpEvent event = new wakeUpEvent(System.currentTimeMillis(), "bed");
        for (Observer o : observers) {
            o.actionOnWakeUp(event);
        }
    }
}

class Dad05 implements Observer {

    public void feed() {
        System.out.println("dad feeding....");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        feed();
    }
}

class Mum05 implements Observer {

    public void hug() {
        System.out.println("mum hugging.........");
    }

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        hug();
    }
}

class Dog05 implements Observer {

    @Override
    public void actionOnWakeUp(wakeUpEvent event) {
        wang();
    }

    public void wang() {
        System.out.println("dog wang....");
    }
}

//为所有的观察者提供统一的接口
interface Observer {
    void actionOnWakeUp(wakeUpEvent event);
}
