# **ReentrantReadWriteLock读写锁**

## 一、锁的分类

这里不会对Java中大部分的分类都聊清楚，主要把 **互斥，共享** 这种分类聊清楚。

Java中的互斥锁，synchronized，ReentrantLock这种都是互斥锁。一个线程持有锁操作时，其他线程都需要等待前面的线程释放锁资源，才能重新尝试竞争这把锁。

Java中的读写锁（支撑互斥&共享），Java中最常见的就是 **ReentrantReadWriteLock** ，StampedLock。

其中StampedLock是JDK1.8中推出的一款读写锁的实现，针对ReentrantReadWriteLock一个优化。但是，今儿不细聊。主要玩ReentrantReadWriteLock。

ReentrantReadWriteLock主要就是解决咱们刚才聊的，读写操作都有，读操作居多，写操作频次相对比较低的情况，可以使用读写锁来提升系统性能。

读写锁中：

* 写写互斥
* 读写互斥
* 写读互斥
* 读读共享
* 有锁降级的情况，后面聊！！

## 二、ReentrantReadWriteLock的基本操作

ReentrantReadWriteLock中实现了ReadWriteLock的接口，在这个接口里面提供了两个抽象方法。

正常的操作，是new ReentrantReadWriteLock的对象，但是你具体的业务操作是需要读锁，还是写锁，你需要单独的获取到，然后针对性的加锁。

```java
public interface ReadWriteLock {
    /**
     * Returns the lock used for reading.
     *
     * @return the lock used for reading
     */
    Lock readLock();

    /**
     * Returns the lock used for writing.
     *
     * @return the lock used for writing
     */
    Lock writeLock();
}
```

具体使用方式

```java
public static void main(String[] args){
    // 1、构建读写锁对象
    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // 2、单独获取读、写锁对象
    ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    // 3、根据业务使用具体的锁对象加锁
    writeLock.lock();
    // try-finally的目的，是为了避免没有及时释放锁资源导致死锁的问题。
    try{
        // 4、业务操作…………
        System.out.println("写操作");
    }finally {
        // 5、释放锁
        writeLock.unlock();
    }
}
```

## 三、ReentrantReadWriteLock的底层实现

ReentrantReadWriteLock是基于AQS实现的。

AQS是JUC包下的一个抽象类AbstractQueuedSynchronizer

暂时只关注两点，分别是AQS提供的state属性，还有AQS提供的一个同步队列。

state属性，用来标识当前 读写锁 的资源是否被占用的核心标识。

```java
private volatile int state;
```

一个int类型的state，是4字节，每个字节占用8个bit位，一个state占用32个bit位。

* 高16位，作为读锁的标记。
* 低16位，作为写锁的标记。

```java
static final int SHARED_SHIFT   = 16;
static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
00000000 00000000 11111111 11111111
/** 查看读锁的占用情况。 */
static int sharedCount(int state)    { return state >>> SHARED_SHIFT; }
/** Returns the number of exclusive holds represented in count  */
static int exclusiveCount(int state) { return state & EXCLUSIVE_MASK; }

00000000 00000000 00000000 00000000    int类型的数值的32个bit位。 

读锁占用情况：
00000000 00000011 00000000 00000000    state
>>> 16 
00000000 00000000 00000000 00000011    读锁被获取了三次。

写锁占用情况。（这里之所以&这个么东西，是对后期的锁降级有影响~）
00000000 00000000 00000000 00000001    state
&
00000000 00000000 11111111 11111111  
=
00000000 00000000 00000000 00000001    写锁被获取了一次。
```

一个同步队列，当线程获取锁资源失败时，需要到这个同步队列中排队。到了合适的时机，就会继续尝试获取对应的锁资源。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/a36102126d7b46469f023c5354473625.png)

## 四、ReentrantReadWriteLock的锁重入

同一个线程，多次获取同一把锁时，就会出现锁重入的情况。

而咱们大多数的锁，都会提供锁重入的功能。

锁重入场景：

```java
public class Demo {
    // 1、构建读写锁对象
    static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // 2、单独获取读、写锁对象
    static ReentrantReadWriteLock.ReadLock readLock;
    static ReentrantReadWriteLock.WriteLock writeLock;

    static{
        // 2、单独获取读、写锁对象
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    public static void main(String[] args){
        // 3、根据业务使用具体的锁对象加锁
        writeLock.lock();
        // try-finally的目的，是为了避免没有及时释放锁资源导致死锁的问题。
        try{
            // 4、业务操作…………调用其他方法
            xxx();
        }finally {
            // 5、释放锁
            writeLock.unlock();
        }
    }

    private static void xxx(){
        writeLock.lock();
        try{
            // 其他按业务
        }finally {
            writeLock.unlock();
        }
    }
}
```

咱们底层的锁重入逻辑很简单

**写锁：** 写锁的实现就是每一次获取写锁时，会对state的低16位+1，再次获取，再次+1。同理，每次释放锁资源时，也需要对state进行-1。 而当对state的低16位减到0时，锁资源就释放干净了。![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/1d7b4571597447c3942d5ae999487f4f.png)

**读锁：** 首先，读锁是共享的，他用state的高16位来维护信息。如果高16位的state的值，经过运算，知道了是4，也就是读锁被获取了4次。可能A线程获取了2次读锁资源。 B线程获取了2次读锁资源。高位的state自然就是4。但是因为程序员写代码除了问题，使用A线程，释放了4次读锁资源，那此时B线程是不是就可能出现数据安全问题了。

所以，为了解决上述的问题，每个线程需要独立的记录自己获取了几次读锁资源。可以使用ThreadLocal来保存线程局部的信息，每次加锁时，ThreadLocal中需要存储一个标记，每次+1。每次释放锁时，也需要将ThreadLocal中的标记进行-1。读线程最后是基于自己的ThreadLocal中的数值，来确认读锁是否释放干净。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/82df5eb46c2047b0b80110b8ca343693.png)![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/d34d60bde6b445af9e23fcf4620b6a3f.png)

## 五、ReentrantReadWriteLock的写锁饥饿

写锁饥饿的问题。

如果写线程在AQS中排队，并且排在head.next的位置。 那么其他想获取读锁的读线程需要排队。避免大量的读请求获取读锁，让写线程一直AQS队列中排队，无法执行写操作的问题。

通过源码可以看到，读写锁中，仅仅针对head.next这个节点的情况，来确认读线程获取读锁时是否需要排队

```java
// 这个方法，总结一句话。  
// AQS中有排队的Node，并且head的next节点是一个有线程并且在等待写锁的Node
final boolean apparentlyFirstQueuedIsExclusive() {
    Node h, s;
    return (h = head) != null &&
        (s = h.next)  != null &&
        !s.isShared()         &&
        s.thread != null;
}
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/95528f6b5ac8482a8845b08ef50507cc.png)

ReentrantReadWriteLock读写锁中有锁降级，但是这个和synchronized的锁升级没任何关系！！！

---

---

---

---

---

## 六、ReentrantReadWriteLock的锁降级

ReentrantReadWriteLock的锁降级是指当前线程如果持有了写锁，可以降级直接获取到读锁。

在读写锁中，持有写锁的同时，再去获取读锁，这种行为一般被称为 **锁降级** 。

在读写锁中，持有读锁的同时，去获取写锁，这种行为被称为 **锁升级** ，这个行为是不允许的。

这里是获取读锁的的逻辑，看一下锁降级的支持方式

```java
// 竞争读锁。
if (exclusiveCount(c) != 0 &&   // 这行代表某个线程持有写锁
    getExclusiveOwnerThread() != current)    // 这行代表持有写锁的不是当前线程
    // 退出竞争，无法获取读锁
    return -1;  
```

前面逻辑没有走return - 1之后，在后续就会正常的对state的高位+1，并且完成读锁的计数操作。

## 七、ReentrantReadWriteLock的优化

ReentrantReadWriteLock的优化主要是在读锁计数层面上做的优化。

这个对性能的优化微乎其微，但是确确实实是一个优化。

在获取读锁时，因为是共享的，这种优化只针对第一个获取读锁的线程和最后一个获取读锁的线程。

针对第一个获取读锁的线程，他采用一个全局变量记录重入次数。这个操作可以节省掉使用ThreadLocal的时间成本和内存成本。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/2746/1715771967085/53be30aeae824495a7bec538a17e2d20.png)

其中firstReader记录第一个获取读锁的线程。

firstReaderHoldCount，记录第一个获取读锁的线程的重入次数。

---

这里是最后一个获取读锁的线程需要走的逻辑

cachedHoldCounter这个属性是记录最后一个获取读锁的线程的重入次数。

这里可以让最后一个获取读锁的线程在重入时，省略掉去ThreadLocal中get计数器的操作，但是之前的set存储操作，不能省略。

```java
// 获取上次最后获取读锁的线程
HoldCounter rh = cachedHoldCounter;
// 查看当前线程是否是之前的cachedHoldCounter
if (rh == null || rh.tid != getThreadId(current))
    // 说明不是，将当前获取读锁的线程设置为cachedHoldCounter
    cachedHoldCounter = rh = readHolds.get();
// 这个判断代表第一次获取读锁才会进去
else if (rh.count == 0)
    // 如果是第一次获取读锁，不是重入，还是需要扔到ThreadLocal里纪录好，。
    readHolds.set(rh);
// 直接对获取到的rh做++操作，代表获取了一次读锁。
rh.count++;
```

## 八、ReentrantReadWriteLock项目落地

1、完成了和数据库查询数据的基本交互

2、完成和优先查询Redis，Redis木有再查询MySQL的基本交互

3、想将数据再前置的JVM缓存中。

* 如果服务基于集群部署，多个JVM缓存之间，可能存在数据不一致的问题
  * 解决方案很多，比如可以做广播，利用MQ实现，甚至Zookeeper做变化感知的通知方式，但是这几种方式都存在一个问题，可能存在不可控的情况，导致某个节点一致存储脏数据。
* 现在换一个方式，基于CAP的考虑，必然无法保证数据的完整一致性。采用AP的效果，允许短时间的内数据出现脏数据的情况。可以基于时间考虑，根据秒数做全量更新（数据体量别太大，数据体量大，不适合做全量更新。）

采用定时任务，做全量更新，给Service提供查询JVM缓存的功能。

代码地址：https://git.mashibing.com/msb_31955/read_write_lock.git
