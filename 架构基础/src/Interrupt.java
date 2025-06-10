public class Interrupt {

//使用interrupt方法中断线程的执行，重点展示了线程在休眠状态下对中断请求的响应机制
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {//当线程未被中断的时候
                System.out.println("thread 执行步骤1：线程即将进入休眠状态");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("thread线程接收到中断指令，执行中断操作");
                    break;
                }
                System.out.println("thread线程执行步骤2：线程执行了任务");
            }
        });
        thread.start();
        Thread.sleep(100);
        System.out.println("主线程：试图终止线程thread");
        thread.interrupt();//调用interrupt方法设置线程的中断标志
    }
}
