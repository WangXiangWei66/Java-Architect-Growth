package observer;


import strategy.Dog;

//加入多个观察者
public class Main04 {

    public static void main(String[] args) {
        Child03 c = new Child03();
        c.wakeUp();
    }
}

class Child03 {
    private boolean cry = false;
    private Dad02 dad = new Dad02();
    private Mum mum = new Mum();
    private Dog02 dog = new Dog02();

    public boolean isCry() {
        return cry;
    }

    public void wakeUp() {
        cry = true;
        dad.feed();
        dog.wang();
        mum.hug();
    }
}

class Dad02 {
    public void feed() {
        System.out.println("dad feeding.......");
    }
}

class Mum {
    public void hug() {
        System.out.println("mum huging....");
    }
}

class Dog02 {
    public void wang() {
        System.out.println("dog wang..........");
    }
}
