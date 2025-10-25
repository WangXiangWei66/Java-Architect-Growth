package observer;

//加入观察者
public class Main03 {

    public static void main(String[] args) {
        Child02 c = new Child02();
        c.wakeUp();
    }
}

class Child02 {
    private boolean cry = false;
    private Dad d = new Dad();//持有观察者的引用

    public boolean isCry() {
        return cry;
    }

    public void wakeUp() {
        cry = true;
        d.feed();
    }
}

class Dad {
    public void feed() {
        System.out.println("dad feeding....");
    }
}
