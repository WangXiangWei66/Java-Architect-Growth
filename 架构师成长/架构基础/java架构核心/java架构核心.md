### 6.3 ThreadPoolExecutor

&emsp;&emsp;系统提供的线程池的实现都有各自的优缺点。我们在实际的使用中更多的是自定义线程池的实现

```java
public ThreadPoolExecutor(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               BlockingQueue<Runnable> workQueue,
                               RejectedExecutionHandler handler) 


```

各个参数的含义：

> corePoolSize：线程池核心线程数量
> maximumPoolSize:线程池最大线程数量
> keepAliverTime：当活跃线程数大于核心线程数时，空闲的多余线程最大存活时间
> unit：存活时间的单位
> workQueue：存放任务的队列
> handler：超出线程范围和队列容量的任务的处理程序

使用的是阻塞队列:

- ArrayBlockingQueue
- LinkedBlockingQueue
- SynchronousQueue
- PriorityBlockingQueue

拒绝策略分类:

- AbortPolicy：直接抛出异常，默认策略
- CallerRunsPolicy：用调用者所在的线程来执行任务
- DiscardOldestPolicy：丢弃阻塞对类中靠最前的任务，并执行当前任务
- DiscardPolicy：直接丢弃任务

提交一个任务到线程池中，线程池的处理流程如下：

1. 判断线程池里的核心线程是否都在执行任务，如果不是（核心线程空闲或者还有核心线程没有被创建）则创建一个新的工作线程来执行任务。如果核心线程都在执行任务，则进入下个流程。
2. 线程池判断工作队列是否已满，如果工作队列没有满，则将新提交的任务存储在这个工作队列里。如果工作队列满了，则进入下个流程。
3. 判断线程池里的线程是否达到最大线程数，如果没有，则创建一个新的工作线程来执行任务。如果已经满了，则交给饱和策略来处理这个任务。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/58551/1751433111001/257b350f642d4de2b58b2238db2a9d6f.png)

### 6.4 线程池调优

要想合理地配置线程池，就必须首先分析任务特性，可以从以下几个角度来分析。

- 任务的性质：CPU密集型任务、IO密集型任务和混合型任务
- 任务的优先级：高中低
- 任务的执行时间：长中短
- 任务 的依赖性;是否依赖其他系统资源
- CPU密集型任务应配置尽可能小的线程，如配置NCPU+1个线程的线程池，
- IO密集型任务线程并不是一直在执行任务 ，则应配置尽可能多的线程，如2*NCPU 。

可以通过Runtime.getRuntime().availableProcessors()方法获得当前设备的CPU个数
优先级不同的任务可以使用优先级队列PriorityBlockingQueue来处理，它可以让优先级高的任务先执行。

## 7.Synchronized

&emsp;&emsp;同步、重量级锁，synchronized可以保证方法或者代码块在运行时，同一时刻只有一个方法可以进入临界区，同时还可以保证共享变量的内存可见性（单台JVM内）

synchronized锁对象:

- 普通同步方法，锁的是当前实例对象
- 静态同步方法，锁的是当前类的class对象
- 同步代码块，锁的是括号里的对象（实例对象或者class对象）

synchronized的锁优化

- 无锁
- 偏向锁:为了在无多线程竞争的情况下尽量减少不必要的轻量级锁执行路径,主要尽可能避免不必须要的CAS操作，如果竞争锁失败，则升级为轻量级锁
- 轻量级锁:自旋方式,该线程等待一段时间，不会被立即挂起，看持有锁的线程是否会很快释放锁（循环方式）
- 重量级锁:阻塞方式
- 锁消除: 有些代码明明不用加锁，结果还给加上锁，这时编译器会判断锁没有什么必要，就直接把锁去掉了
- 锁粗化:将多个连续的加锁、解锁操作连接在一起，扩展成一个范围更大的锁。例如for循环内部获取锁

## 8.AQS

AbstractQueuedSynchronizer，同步器，实现JUC核心基础组件,解决了子类实现同步器时涉及到的大量细节性问题，例如：同步状态、FIFO同步队列等,采用**模板方法**模式，AQS**实现了大量的通用方法**，子类通过继承方式实现其抽象方法来管理同步状态

同步锁获取与释放:

- 独占式
- 共享式

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/58551/1751433111001/5d3fcb0063c94bdea29015b5d8dfaf80.png)

https://www.processon.com/view/link/5f805d597d9c0806f26a1533

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/58551/1751433111001/2a5fc26a594f4f4db321dc8afcf3e4e8.png)

## 9.ThreadLocal

&emsp;&emsp;一种解决多线程环境下成员变量的问题的方案，但是与线程同步无关，其思路就是为每个线程创建一个单独的变量副本。从而每个线程都可以独立的改变自己所拥有的变量副本，而不会影响其他线程对应的变量副本 , ThreadLocal不是用于解决共享变量的问题，也不是为了协调线程同步而存在，而是为了方便每个线程处理自己的状态而引入的一个机制

&emsp;&emsp;ThreadLocal可以理解为线程本地变量，他会在每个线程都创建一个副本，那么在线程之间访问内部副本变量就行了，做到了线程之间互相隔离，相比于synchronized的做法是用空间来换时间。

&emsp;&emsp;ThreadLocal有一个静态内部类ThreadLocalMap，ThreadLocalMap又包含了一个Entry数组，Entry本身是一个弱引用，他的key是指向ThreadLocal的弱引用，Entry具备了保存key value键值对的能力。

&emsp;&emsp;弱引用的目的是为了防止内存泄露，如果是强引用那么ThreadLocal对象除非线程结束否则始终无法被回收，弱引用则会在下一次GC的时候被回收。

&emsp;&emsp;但是这样还是会存在内存泄露的问题，假如key和ThreadLocal对象被回收之后，entry中就存在key为null，但是value有值的entry对象，但是永远没办法被访问到，同样除非线程结束运行。

&emsp;&emsp;但是只要ThreadLocal使用恰当，在使用完之后调用remove方法删除Entry对象，实际上是不会出现这个问题的。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/58551/1751433111001/8bb7a6e55031438c8223da92287f6940.png)
