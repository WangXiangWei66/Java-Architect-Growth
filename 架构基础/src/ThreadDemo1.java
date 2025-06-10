import java.util.concurrent.CountDownLatch;


//ThreadDemo1继承自Thread类，使他成为一个线程类
public class ThreadDemo1 extends Thread {
    //countDownLatch用于线程间的同步，他允许一个或多个线程等待其他线程完成操作
    CountDownLatch countDownLatch;

    public ThreadDemo1(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override//标记这个方法是重写父类的方法
    public void run() {
        try {
            //每个线程都会休眠两秒
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + ":my thread ");
            //当其他线程调用Thread.interrupt()中断当前线程的休眠时，sleep会抛出InterruptedException。
            //当前实现仅打印堆栈跟踪，线程会继续执行后续代码（包括finally块）。
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //保证无论线程是否中断，都会调用countDown，计数器归零后，会被唤醒
            countDownLatch.countDown();//countDown将计数器-1
        }
    }

    public static void main(String[] args) {
        //将计数器初始化为2，表示等待2个线程完成
        CountDownLatch countDownLatch1 = new CountDownLatch(2);
        for (int i = 0; i < 2; i++) {
            ThreadDemo1 myThread1 = new ThreadDemo1(countDownLatch1);
            myThread1.start();
        }

        try {
            //await使得主线程阻塞，直到计数器变为0
            countDownLatch1.await();
            //当两个子线程都调用countDown后，主线程继续执行并打印完成信息
            System.out.println("thread complete...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

