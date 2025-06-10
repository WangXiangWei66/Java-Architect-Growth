import java.util.concurrent.CountDownLatch;

public class ThreadDemo2 implements Runnable {
    CountDownLatch countDownLatch;

    public ThreadDemo2(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + ":my runnable");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args) {
        CountDownLatch countDownLatch2 = new CountDownLatch(2);
        ThreadDemo2 myRunnable = new ThreadDemo2(countDownLatch2);
        for (int i = 0; i < 2; i++) {
            new Thread(myRunnable).start();
        }

        try {
            countDownLatch2.await();
            System.out.println("runnable complete...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行代码的流程：
     * 主线程创建CountDownLatch，计数器初始化为2
     * 创建并启动两个线程
     * 主线程调用await进入等待状态
     * 子线程各自休眠两秒
     * 子线程打印消息后第哦啊用countDown
     * 当两个子线程都调用countDown后，计数器变为0
     * 主线程恢复执行，打印结束
     */
}
