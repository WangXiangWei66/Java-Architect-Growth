package observer;

//面向对象的傻等
public class Main02 {

    public static void main(String[] args) {
        //创建被观察的对象
        Child child = new Child();
        while (!child.isCry()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("observing....");
        }
    }
}

class Child {
    private boolean cry = false;

    public boolean isCry() {
        return cry;
    }

    public void wakeup() {
        System.out.println("Wake Up! Crying wuwuwuwuwu......");
        cry = true;
    }
}
