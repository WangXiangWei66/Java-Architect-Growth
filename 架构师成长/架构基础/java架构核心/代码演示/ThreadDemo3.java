//Callable：用于定义带返回值的任务（对比无返回值的Runnable）
import java.util.concurrent.Callable;
//ExecutionExecution：当任务执行抛出异常时，通过futureTask.get()抛出此异常
import java.util.concurrent.ExecutionException;
//FutureTask:用于包装Callable任务，实现Future接口，可获取任务结果或者取消任务
import java.util.concurrent.FutureTask;

public class ThreadDemo3 implements Callable<Integer> {

    public static void main(String[] args) {
        ThreadDemo3 threadDemo3 = new ThreadDemo3();
        //用futureTask接受结果，可做欸Thread的目标对象启动线程，又能通过Future接口获取结果
        FutureTask<Integer> futureTask = new FutureTask<>(threadDemo3);
        new Thread(futureTask).start();

        try {
            //阻塞等待线程，直到任务完成
            Integer sum = futureTask.get();
            System.out.println("sum=" + sum);
            System.out.println("----------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override//这个方法可以抛出异常，且有返回值
    //该方法是单线程执行，无需考虑线程安全
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 101; i++) {
            sum += i;
        }
        return sum;
    }
}
