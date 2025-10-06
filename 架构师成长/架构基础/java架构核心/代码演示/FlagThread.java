public class FlagThread extends Thread {

    //通过volatile标志位interrupt控制线程的循环执行，并尝试处理线程中断
    public volatile boolean inInterrupt = false;

    @Override
    public void run() {
        while (!inInterrupt) {
            System.out.println("业务逻辑1....");
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("业务逻辑2...");
        }
    }
}
