package singleton;

/**
 * lazy loading
 * 也称懒汉式
 * 虽然达到了按需初始化的目的，但却带来线程不安全的问题
 * 可以通过synchronized解决，但也带来效率下降
 */
public class Mgr06 {

    //存储单例实现，但是未及时初始化
    public static volatile Mgr06 INSTANCE;//volatile关键字，防止指令重排序

    private Mgr06() {
    }

    //获取单例的实现，延迟初始化
    //双重检查+volatile+同步保证安全与性能
    public static Mgr06 getInstance() {
        if (INSTANCE == null) {
            //双重检查
            //保证同一时间只有一个线程会进入临界区
            synchronized (Mgr06.class) {
                if (INSTANCE == null) {
                    try {
                        Thread.sleep(1);//增加线程并发问题出现的概率
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    INSTANCE = new Mgr06();
                }
            }
        }
        return INSTANCE;
    }

    public void m() {
        System.out.println("m");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() ->
                    System.out.println(Mgr06.getInstance().hashCode())).start();
        }
    }
}
