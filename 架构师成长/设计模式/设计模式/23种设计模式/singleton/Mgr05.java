package singleton;

/**
 * lazy loading
 * 也称懒汉式
 * 虽然达到了按需初始化的目的，但却带来线程不安全的问题
 * 可以通过synchronized解决，但也带来效率下降
 */
public class Mgr05 {

    //存储单例实现，但是未及时初始化
    public static Mgr05 INSTANCE;

    private Mgr05() {
    }

    //获取单例的实现，延迟初始化
    //添加synchronized关键字来保证线程安全
    public static Mgr05 getInstance() {
        if (INSTANCE == null) {
            //妄图通过减小同步代码块的方式来提高效率，方法并不可行
            synchronized (Mgr05.class) {
                try {
                    Thread.sleep(1);//增加线程并发问题出现的概率
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                INSTANCE = new Mgr05();
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
                    System.out.println(Mgr05.getInstance().hashCode())).start();
        }
    }
}
