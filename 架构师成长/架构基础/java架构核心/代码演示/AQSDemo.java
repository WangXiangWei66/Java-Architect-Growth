//本代码演示了ReentrantLock实现多线程同步，通过countDownLatch确保主线程等待所有子线程执行完毕后输出结果
import java.util.concurrent.CountDownLatch;//用于线程同步
//用于实现锁机制
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AQSDemo {

    //共享的资源，static修饰符使其成为类变量，所有线程共享
    private static int count = 0;
    //创建可重入锁实例
    private static Lock lock = new ReentrantLock();

    /**
     * 造成数据安全问题的原因：
     * 1.原子性：执行的最小单元，要么都执行，要么都不执行
     * 2.可见性
     * 3.有序性
     */
    //定义一个方法去操作资源
    public static void incr() {
        try {
            Thread.sleep(10);//增加切换线程的可能性
            lock.lock();//加锁
            lock.lock();
            count++;//原子，可见性
        } catch (Exception e) {

        } finally {
            lock.unlock();
            lock.unlock();//释放锁
        }
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(100);
        //多个线程去操作共享资源
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    incr();
                    latch.countDown();
                }
            }).start();
        }
        latch.await();//阻塞
        //获取共享的数据
        System.out.println("count = " + count);
    }
}
