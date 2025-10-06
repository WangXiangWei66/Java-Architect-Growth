# **FutureTask底层实现**

## 一、FutureTask的基本使用

平时一些业务需要做并行处理，正常如果你没有返回结果的需求，直接上Runnable。

很多时候咱们是需要开启一个新的线程执行任务后，给我一个返回结果。此时咱们需要使用Callable。

在使用Callable的时候，一般就会配合FutureTask去使用。

FutureTask在构建时，需要基于有参构造将Callable任务传递到Future中，在给线程提交任务时，提交的是FutureTask，不过Thread对象，值提供了传递Runnable（任务）的有参构造。紧接着查看FutureTask的结构，会发现FutureTask实现了RunnableFuture的接口，RunnableFuture继承了Runnable。所以本质上来说，FutureTask也是Runnable类型。

基本使用方式：

```java
public static void main(String[] args)  {
    // 封装一个Callable的任务，扔到FutureTask中
    Callable callable = new Callable() {
        @Override
        public Object call() throws Exception {
            Thread.sleep(3000);
//                异常结束
//                int i = 1 / 0;
            double b = Math.random();
            return b;
        }
    };
    FutureTask task = new FutureTask(callable);

    // 构建线程，并且传递Callable的任务
    Thread t = new Thread(task);

    // 启动线程
    t.start();

    // 主线程获取子线程中callable的任务结果
    try {
        Object o = task.get();
        System.out.println("任务执行没异常。" + o);
    } catch (Exception e) {
        System.out.println("任务执行有异常。" + e);
    }

}
```

## 二、FutureTask任务状态的流转

FutureTask中，提供了很多种任务状态

```java
private volatile int state;
private static final int NEW          = 0;    // 刚new任务
private static final int COMPLETING   = 1;    // 任务开始跑！
private static final int NORMAL       = 2;    // 正常结束，返回结果
private static final int EXCEPTIONAL  = 3;    // 异常结束，返回异常
private static final int CANCELLED    = 4;    // 任务取消，需要自己调用FutureTask提供的API
private static final int INTERRUPTING = 5;    // 任务中断ing，根据线程走中断。。
private static final int INTERRUPTED  = 6;    // 任务中断了。
```

FutureTask任务的流转过程有这四种可能：

```java
NEW -> COMPLETING -> NORMAL
NEW -> COMPLETING -> EXCEPTIONAL
NEW -> CANCELLED
NEW -> INTERRUPTING -> INTERRUPTED
```

了解了这几个状态和状态流转的过程后，再查看源码中的几个核心属性

```java
/** 传递给FutureTask的Callable，存这！ */
private Callable<V> callable;
/** 存储返回结果的，正常返回和异常返回信息都存这。 */
private Object outcome; 
/** 运行任务滴内个线程    执行任务的子线程*/
private volatile Thread runner;
/** 这里是排队等待结果的线程存储位置     等到结果的主线程*/
private volatile WaitNode waiters;
```

## 三、FutureTask中任务的执行

因为需要启动线程来执行FutureTask提供的任务。

而启动线程就是走Thread对象的start方法。

start方法就会调用有参构造传入的Runnable的run方法。

FutureTask是Runnable的实现类，自然也需要重写run方法。

就是查看FutureTask的run方法。

---

分析源码

> 任务执行时，会优先判断当前FutureTask的状态是否正确。（需要是NEW状态）
>
> 会将runner属性，基于CAS从null修改为当前线程。
>
> 执行callable的call方法。
>
> 两种返回方式：
>
> * 正常返回，执行set方法，封装返回结果
> * 异常返回，执行setException方法，封装异常信息

## 四、FutureTask中返回结果的封装

因为FutureTask的任务可以正常返回，也有异常返回。

在正常返回时，执行set方法。

> set方法第一步是基于CAS的方式，将任务状态从NEW，修改为COMPLETING，修改成功后，就会将返回结果赋值给outcome。最后再将状态从COMPLETING修改为NORMAL，代表任务正常结束了。

在异常返回时，执行setException方法

> set方法第一步是基于CAS的方式，将任务状态从NEW，修改为COMPLETING，修改成功后，就会将返回的异常结果赋值给outcome。最后再将状态从COMPLETING修改为EXCEPTIONAL，代表任务异常结束。

**线程池执行任务时，如果任务出现异常，会发生什么？**

* 线程结束的方式。   run方法结束（正常结束、异常结束）
* Runnable和FutureTask处理异常的方式。
  * Runnable的异常会直接抛出来给线程
  * FutureTask的异常会基于setException存储到outcome中。

## 五、FutureTask中任务取消的操作

cancel就是FutureTask提供的一个取消任务的方法。

这个方法中有一个参数，mayInterruptIfRunning。

如果传递true，会将执行任务的线程做interrupt方法，但是这个执行任务的线程能否及时停住，不一定，要看具体业务代码怎么写的。

如果传递false，不会中断，只会尝试修改状态

查看源码：

> cancel只能在状态为NEW的前提下，进行CAS修改状态，如果mayInterruptIfRunning为true，状态修改为INTERRUPTING，如果mayInterruptIfRunning为false，就修改为CANCEL。
>
> 如果前面两个操作成功，取消任务成功，反之直接返回false，代表取消失败，任务已经执行成功了。
>
> 如果mayInterruptIfRunning为true，会在后面拿到执行任务的线程，在不为null的前提下，执行线程的interrupt方法，中断线程。

## 六、FutureTask获取结果的方式

其他线程要获取FutureTask的返回结果时，会执行get方法。

在get方法中，首先就是查看当前任务的状态是否完成，如果没完成，需要挂起当前等待任务的线程。

任务没完整，需要走awaitDone的逻辑。

在死循环中：   **是一堆if else，每次循环只走一个逻辑。**

* 查看任务状态是否完成，完成就退出awaitDone逻辑。
* 如果任务状态是COMPLETING，那就yield稍等一会，因为任务马上完成！
* 查看当前线程是否被中断，如果被中断，最好后续处理，然后直接抛异常！
* 封装WaitNode
  * 如果使用get方法时，指定了等待时间，先查看时间到了没，到了就不等了，返回。
  * 正常将当前线程封装为WaitNode。
* 如果WaitNode还没添加到队列排队，这里就将声明好的WaitNode排队进来。
* 如果get方法指定了等待时间，在这里计算好等待的时间，并且任务没处理完，直接park(时间)挂起
* 前面都没走，最后在这，直接park无限期挂起，等待被唤醒。

最后会根据任务完成的状态做不同的操作。

* NORMAL：正常返回结果
* EXCEPTIONAL：将setException中存储的异常直接抛出。
* 大于CANCELLED：直接抛出CancellationException异常

## 七、FutureTask任务完成后的唤醒操作

在任务完成后，无论是正常的完成，还是异常完成，还是说被取消的情况，最终都会执行finishCompletion方法，去唤醒所有排队的WaitNode节点。

执行finishCompletion的第一步操作，就是拿到当前正在排队的waiters这个单向链表。

需要基于CAS将单向链表的waiters的引用置位null（help GC）。这里是个死循环，如果第一次CAS失败，会再次CAS。

然后将waiters中的单向链表的所有WaitNode中的线程统统滴unpark唤醒。

同时FutureTask也提供了一个done操作，可以在任务完成后，做一个你希望做的事，这个方法需要你自己重写

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1716204572084/9e8a50894164433380ff7676c6b14195.png)

---

---

---

## 八、FutureTask的实战应用

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1716204572084/e060872926ce4cf084cd9d0ffb17dc14.png)

**设计思路**

首先推送的操作，必然要换成多线程的方式推送，来提升效率。

* 多线程查询MySQL数据的问题
  * 每个线程都需要查询自己的数据，需要做好数据的分片，让多个线程同时查询需要的数据，要并行处
  * 再考虑单纯使用limit可以能存在数据漏查或者是重复数据的推送情况因为咱们的主键是自增的，1~+∞，直接基于主键做好分片，让每个线程查询自己范围内的数据即可。
  * 同时也可以规避掉深分页查询数据慢的情况。
* 多线程推送时，可能出现数据推送失败的情况。需要将推送失败的数据自己留存好，方便后期做补偿或者重试的操作。
* 多线程方向，正常采用ThreadPoolExecutor的方式，将自己分片好的任务投递给线程池，现在数据体量预估最高50W条左右，提供50个线程，每个线程处理10000条数据。 **（现在是暂时这么决定，后面必然要根据业务的情况尝试动态调整线程池参数）**

干活！！！

1、先提供查询的SQL语句，需要提供两个

* 查询当前需要推送数据的总条数
* 基于id作为条件分片查询数据

```java
public interface AirMapper {

    @Select("select * from air")
    List<Air> findAll();

    // 提供一个查询需要推送数据的SQL总条数
    @Select("select count(1) from air where monitor_time = #{monitorTime}")
    long selectCountNeedPush(@Param("monitorTime")String monitorTime);

    // 查询今日第一个需要推送的数据,目的是获取ID方便计算
    @Select("select id from air where monitor_time = #{monitorTime} order by id limit 1")
    Long selectOneNeedPush(@Param("monitorTime")String monitorTime);

    // 查询需要推送的数据的内容，基于ID范围去查询。
    @Select("select * from air where monitor_time = #{monitorTime} and id between #{fromId} and #{toId} order by id")
    List<Air> selectByIdAndLimit(@Param("monitorTime")String monitorTime,@Param("fromId") long fromId,@Param("toId") long toId);
}
```

2、修改之前的Service业务，将串行优化为并行。

代码地址：https://git.mashibing.com/msb_31955/furtureTask.git
