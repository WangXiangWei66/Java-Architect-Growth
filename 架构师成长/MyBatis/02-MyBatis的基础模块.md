# MyBatis的缓存和日志模块


# 一、缓存模块

MyBatis作为一个强大的持久层框架，缓存是其必不可少的功能之一，Mybatis中的缓存分为一级缓存和二级缓存。但本质上是一样的，都是使用Cache接口实现的。缓存位于 org.apache.ibatis.cache包下。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/8d1f788dce9943b5aa05f35be03b1b2c.png)

通过结构我们能够发现Cache其实使用到了装饰器模式来实现缓存的处理。首先大家需要先回顾下装饰器模式的相关内容哦。我们先来看看Cache中的基础类的API

> // 煎饼加鸡蛋加香肠
> “装饰者模式（Decorator Pattern）是指在不改变原有对象的基础之上，将功能附加到对象上，提供了比继承更有弹性的替代方案（扩展原有对象的功能）。”

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/3fd273b917e84ff88a87848ec4f46350.png)

## 1. Cache接口

```
Cache接口是缓存模块中最核心的接口，它定义了所有缓存的基本行为，Cache接口的定义如下:
```

```java
public interface Cache {

  /**
   * 缓存对象的 ID
   * @return The identifier of this cache
   */
   //返回当前缓存对象的唯一标识符，用于区分不同的缓存实例
  String getId();

  /**
   * 向缓存中添加数据，一般情况下 key是CacheKey  value是查询结果
   * @param key Can be any object but usually it is a {@link CacheKey}
   * @param value The result of a select.
   */
   //向缓存中添加数据。参数 key 通常是一个 CacheKey 类型的对象，用于标识缓存数据；value 则是要缓存的数据（通常是查询结果）。
  void putObject(Object key, Object value);

  /**
   * 根据指定的key，在缓存中查找对应的结果对象
   * @param key The key
   * @return The object stored in the cache.
   */
   //根据指定的 key 从缓存中获取对应的数据。如果缓存中没有该 key 对应的数据，可能返回 null。
  Object getObject(Object key);

  /**
   * As of 3.3.0 this method is only called during a rollback
   * for any previous value that was missing in the cache.
   * This lets any blocking cache to release the lock that
   * may have previously put on the key.
   * A blocking cache puts a lock when a value is null
   * and releases it when the value is back again.
   * This way other threads will wait for the value to be
   * available instead of hitting the database.
   *   删除key对应的缓存数据
   *
   * @param key The key
   * @return Not used
   */
   //删除缓存中指定 key 对应的数据。特别说明：从 3.3.0 版本开始，此方法仅在回滚（rollback）时调用，用于处理之前缓存中缺失的值，让阻塞式缓存释放可能对该键施加的锁。
  Object removeObject(Object key);

  /**
   * Clears this cache instance.
   * 清空缓存
   */
   //清空当前缓存实例中的所有数据。
  void clear();

  /**
   * Optional. This method is not called by the core.
   * 缓存的个数。
   * @return The number of elements stored in the cache (not its capacity).
   */
   //可选方法，返回缓存中存储的元素数量（不是缓存的容量）。核心系统不会调用此方法。
  int getSize();

  /**
   * Optional. As of 3.2.6 this method is no longer called by the core.
   * <p>
   * Any locking needed by the cache must be provided internally by the cache provider.
   *  获取读写锁
   * @return A ReadWriteLock
   */
   //可选方法，返回一个读写锁。从 3.2.6 版本开始，核心系统不再调用此方法，缓存所需的任何锁定机制都必须由缓存提供者在内部实现。这是一个默认方法（Java 8 及以上支持），默认返回 null。
  default ReadWriteLock getReadWriteLock() {
    return null;
  }

}
```

Cache接口的实现类很多，但是大部分都是装饰器，只有PerpetualCache提供了Cache接口的基本实现。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/38de80b0016545299285ba661a4de0ab.png)

## 2. PerpetualCache

PerpetualCache在缓存模块中扮演了ConcreteComponent的角色，其实现比较简单，底层使用HashMap记录缓存项，具体的实现如下：

```java
/**
 * 在装饰器模式用 用来被装饰的对象
 * 缓存中的  基本缓存处理的实现
 * 其实就是一个 HashMap 的基本操作
 * @author Clinton Begin
 */
public class PerpetualCache implements Cache {

  private final String id; // Cache 对象的唯一标识

  // 用于记录缓存的Map对象
  private final Map<Object, Object> cache = new HashMap<>();

  public PerpetualCache(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public int getSize() {
    return cache.size();
  }

  @Override
  public void putObject(Object key, Object value) {
    cache.put(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return cache.get(key);
  }

  @Override
  public Object removeObject(Object key) {
    return cache.remove(key);
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (getId() == null) {
      throw new CacheException("Cache instances require an ID.");
    }
    if (this == o) {
      return true;//判断两个缓存的内存地址是否相同
    }
    if (!(o instanceof Cache)) {
      return false;//检查cache对象是否为Cache类型的实例
    }

    Cache otherCache = (Cache) o;
    // 只关心ID
    return getId().equals(otherCache.getId());
  }

  @Override
  public int hashCode() {
    if (getId() == null) {
      throw new CacheException("Cache instances require an ID.");
    }
    // 只关心ID
    return getId().hashCode();
  }

}

```

然后我们可以来看看cache.decorators包下提供的装饰器。他们都实现了Cache接口。这些装饰器都在PerpetualCache的基础上提供了一些额外的功能，通过多个组合实现一些特殊的需求。

## 3.BlockingCache

通过名称我们能看出来是一个阻塞同步的缓存，它保证只有一个线程到缓存中查找指定的key对应的数据。

```java
public class BlockingCache implements Cache {

  private long timeout; // 阻塞超时时长
  private final Cache delegate; // 被装饰的底层 Cache 对象
  // 每个key 都有对象的 ReentrantLock 对象
  private final ConcurrentHashMap<Object, ReentrantLock> locks;

  public BlockingCache(Cache delegate) {
    // 被装饰的 Cache 对象
    this.delegate = delegate;
    this.locks = new ConcurrentHashMap<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public void putObject(Object key, Object value) {
    try {
      // 执行 被装饰的 Cache 中的方法
      delegate.putObject(key, value);
    } finally {
      // 释放锁
      releaseLock(key);
    }
  }

  @Override
  public Object getObject(Object key) {
    acquireLock(key); // 获取锁
    Object value = delegate.getObject(key); // 获取缓存数据
    if (value != null) { // 有数据就释放掉锁，否则继续持有锁
      releaseLock(key);
    }
    return value;
  }

  @Override
  public Object removeObject(Object key) {
    // despite of its name, this method is called only to release locks
    releaseLock(key);
    return null;
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  private ReentrantLock（可重入锁） getLockForKey(Object key) {
    return locks.computeIfAbsent(key, k -> new ReentrantLock());
  }

  private void acquireLock(Object key) {
    Lock lock = getLockForKey(key);
    if (timeout > 0) {
      try {
        boolean acquired = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        if (!acquired) {
          throw new CacheException("Couldn't get a lock in " + timeout + " for the key " +  key + " at the cache " + delegate.getId());
        }
      } catch (InterruptedException e) {
        throw new CacheException("Got interrupted while trying to acquire lock for key " + key, e);
      }
    } else {
      lock.lock();//会一致阻塞在这里，直到获取到锁为止
    }
  }

  private void releaseLock(Object key) {
    ReentrantLock lock = locks.get(key);
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
    }
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
}

```

通过源码我们能够发现，BlockingCache本质上就是在我们操作缓存数据的前后通过 ReentrantLock对象来实现了加锁和解锁操作。其他的具体实现类，大家可以自行查阅

| 缓存实现类         | **描述**   | **作用**                                                                                                                               | 装饰条件                                       |
| ------------------ | ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| 基本缓存           | 缓存基本实现类   | 默认是PerpetualCache，也可以自定义比如RedisCache、EhCache等，具备基本功能的缓存类                                                            | 无                                             |
| LruCache           | LRU策略的缓存    | 当缓存到达上限时候，删除最近最少使用的缓存（Least Recently Use）                                                                             | eviction="LRU"（默认）                         |
| FifoCache          | FIFO策略的缓存   | 当缓存到达上限时候，删除最先入队的缓存                                                                                                       | eviction="FIFO"                                |
| SoftCacheWeakCache | 带清理策略的缓存 | 通过JVM的软引用和弱引用来实现缓存，当JVM内存不足时，会自动清理掉这些缓存，基于SoftReference和WeakReference                                   | eviction="SOFT"eviction="WEAK"                 |
| LoggingCache       | 带日志功能的缓存 | 比如：输出缓存命中率                                                                                                                         | 基本                                           |
| SynchronizedCache  | 同步缓存         | 基于synchronized关键字实现，解决并发问题                                                                                                     | 基本                                           |
| BlockingCache      | 阻塞缓存         | 通过在get/put方式中加锁，保证只有一个线程操作缓存，基于Java重入锁实现                                                                        | blocking=true                                  |
| SerializedCache    | 支持序列化的缓存 | 将对象序列化以后存到缓存中，取出时反序列化                                                                                                   | readOnly=false（默认）                         |
| ScheduledCache     | 定时调度的缓存   | 在进行get/put/remove/getSize等操作前，判断缓存时间是否超过了设置的最长缓存时间（默认是一小时），如果是则清空缓存--即每隔一段时间清空一次缓存 | flushInterval不为空                            |
| TransactionalCache | 事务缓存         | 在二级缓存中使用，可一次存入多个缓存，移除多个缓存                                                                                           | 在TransactionalCacheManager中用Map维护对应关系 |

## 4. 缓存的应用

### 4.1 缓存对应的初始化

在Configuration初始化的时候会为我们的各种Cache实现注册对应的别名

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/50ffb825094147749ab297b0787f9b1f.png)

```
在解析settings标签的时候，设置的默认值有如下
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/4a9e32897b3f4a3ba92e42465ef348ee.png)

cacheEnabled默认为true，localCacheScope默认为 SESSION

在解析映射文件的时候会解析我们相关的cache标签

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/bc50eff0e5554866880dc0105fd7c6b3.png)

然后解析映射文件的cache标签后会在Configuration对象中添加对应的数据在

```java
//解析缓存配置，并应用到构建器中
private void cacheElement(XNode context) {//接受了一个代表XML配置的缓存节点
    // 只有 cache 标签不为空才解析
    if (context != null) {
      String type = context.getStringAttribute("type", "PERPETUAL");//获取缓存类型，默认是永久缓存
      Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
      String eviction = context.getStringAttribute("eviction", "LRU");//设置缓存回收策略
      Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
      Long flushInterval = context.getLongAttribute("flushInterval");//刷新间隔
      Integer size = context.getIntAttribute("size");//缓存容量
      boolean readWrite = !context.getBooleanAttribute("readOnly", false);//是否可读写
      boolean blocking = context.getBooleanAttribute("blocking", false);//是否阻塞
      Properties props = context.getChildrenAsProperties();//其他的可拓展性
      builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);//将解析到的缓存配置应用到构建器中，用于后续创建缓存实例
    }
  }
```

继续

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/e7e27c6c7ec0402aa588057f3ffc39d6.png)

然后我们可以发现 如果存储 cache 标签，那么对应的 Cache对象会被保存在 currentCache 属性中。

进而在 Cache 对象 保存在了 MapperStatement 对象的 cache 属性中。

然后我们再看看openSession的时候又做了哪些操作，在创建对应的执行器的时候会有缓存的操作

```java
//根据指定的执行器类型创建相应的Excutor实例，并进行增强处理
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    //首先确认执行器的类型
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);//批处理执行器
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);//可重用执行器
    } else {
      // 默认 SimpleExecutor（简单执行器）
      executor = new SimpleExecutor(this, transaction);
    }
    // 二级缓存开关，settings 中的 cacheEnabled 默认是 true
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    // 植入插件的逻辑，至此，四大对象已经全部拦截完毕
    executor = (Executor) interceptorChain.pluginAll(executor);//为执行器植入所有注册的插件
    return executor;
  }
```

也就是如果 cacheEnabled 为 true 就会通过 CachingExecutor 来装饰executor 对象，然后就是在执行SQL操作的时候会涉及到缓存的具体使用。这个就分为一级缓存和二级缓存，这个我们来分别介绍

### 4.2 一级缓存

一级缓存也叫本地缓存（Local Cache），MyBatis的一级缓存是在会话（SqlSession）层面进行缓存的。MyBatis的一级缓存是默认开启的，不需要任何的配置（如果要关闭，localCacheScope设置为STATEMENT）。在BaseExecutor对象的query方法中有关闭一级缓存的逻辑

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/780ec5e0e2ee481e91fefbdf80de64b3.png)

```
然后我们需要考虑下在一级缓存中的 PerpetualCache 对象在哪创建的，因为一级缓存是Session级别的缓存，肯定需要在Session范围内创建，其实PerpetualCache的实例化是在BaseExecutor的构造方法中创建的
```

```java
//传入配置对象和事务对象  
protected BaseExecutor(Configuration configuration, Transaction transaction) {
    this.transaction = transaction;
    this.deferredLoads = new ConcurrentLinkedQueue<>();//配置MyBatis的延迟加载机制
    this.localCache = new PerpetualCache("LocalCache");
    this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");//输出参数缓存
    this.closed = false;//执行器未关闭
    this.configuration = configuration;
    this.wrapper = this;//用于装饰器模式中的自我引用
  }
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/f2225013abcb41d7a2569b00f030cf07.png)

一级缓存的具体实现也是在BaseExecutor的query方法中来实现的

```java
//负责数据库查询的和核心流程，包含缓存处理和查询执行
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    // 异常体系之 ErrorContext，用来记录当前查询的资源、操作和对象信息
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      // flushCache="true"时，即使是查询，也清空一级缓存
      clearLocalCache();
    }
    List<E> list;
    try {
      // 防止递归查询重复处理缓存
      queryStack++;
      // 查询一级缓存
      // ResultHandler 和 ResultSetHandler的区别
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        // 真正的查询流程
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      queryStack--;
    }
    //处理延迟加载内容，并清空延迟加载列表
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/753ee4f77277487698d5f3b94c120830.png)

一级缓存的验证：

同一个Session中的多个相同操作

```java
    @Test
    public void test1() throws  Exception{
        // 1.获取配置文件，里面包含了数据库连接信息、映射文件路径等配置
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        // 2.加载解析配置文件并获取SqlSessionFactory对象
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        // 3.根据SqlSessionFactory对象获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        // 4.通过SqlSession中提供的 API方法来操作数据库
        List<User> list = sqlSession.selectList("com.gupaoedu.mapper.UserMapper.selectUserList");
        System.out.println(list.size());
        // 一级缓存测试
        System.out.println("---------");
        list = sqlSession.selectList("com.gupaoedu.mapper.UserMapper.selectUserList");
        System.out.println(list.size());
        // 5.关闭会话
        sqlSession.close();
    }
```

输出日志

```txt
Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
==>  Preparing: select * from t_user 
==> Parameters: 
<==    Columns: id, user_name, real_name, password, age, d_id
<==        Row: 1, zhangsan, 张三, 123456, 18, null
<==        Row: 2, lisi, 李四, 11111, 19, null
<==        Row: 3, wangwu, 王五, 111, 22, 1001
<==        Row: 4, wangwu, 王五, 111, 22, 1001
<==        Row: 5, wangwu, 王五, 111, 22, 1001
<==        Row: 6, wangwu, 王五, 111, 22, 1001
<==        Row: 7, wangwu, 王五, 111, 22, 1001
<==        Row: 8, aaa, bbbb, null, null, null
<==        Row: 9, aaa, bbbb, null, null, null
<==        Row: 10, aaa, bbbb, null, null, null
<==        Row: 11, aaa, bbbb, null, null, null
<==        Row: 12, aaa, bbbb, null, null, null
<==        Row: 666, hibernate, 持久层框架, null, null, null
<==      Total: 13
13
---------
13
```

可以看到第二次查询没有经过数据库操作

不同Session的相同操作

```java
    @Test
    public void test2() throws  Exception{
        // 1.获取配置文件
        InputStream in = Resources.getResourceAsStream("mybatis-config.xml");
        // 2.加载解析配置文件并获取SqlSessionFactory对象
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        // 3.根据SqlSessionFactory对象获取SqlSession对象
        SqlSession sqlSession = factory.openSession();
        // 4.通过SqlSession中提供的 API方法来操作数据库
        List<User> list = sqlSession.selectList("com.gupaoedu.mapper.UserMapper.selectUserList");
        System.out.println(list.size());
        sqlSession.close();
        sqlSession = factory.openSession();
        // 一级缓存测试
        System.out.println("---------");
        list = sqlSession.selectList("com.gupaoedu.mapper.UserMapper.selectUserList");
        System.out.println(list.size());
        // 5.关闭会话
        sqlSession.close();
    }
```

输出结果

```txt
Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
==>  Preparing: select * from t_user 
==> Parameters: 
<==    Columns: id, user_name, real_name, password, age, d_id
<==        Row: 1, zhangsan, 张三, 123456, 18, null
<==        Row: 2, lisi, 李四, 11111, 19, null
<==        Row: 3, wangwu, 王五, 111, 22, 1001
<==        Row: 4, wangwu, 王五, 111, 22, 1001
<==        Row: 5, wangwu, 王五, 111, 22, 1001
<==        Row: 6, wangwu, 王五, 111, 22, 1001
<==        Row: 7, wangwu, 王五, 111, 22, 1001
<==        Row: 8, aaa, bbbb, null, null, null
<==        Row: 9, aaa, bbbb, null, null, null
<==        Row: 10, aaa, bbbb, null, null, null
<==        Row: 11, aaa, bbbb, null, null, null
<==        Row: 12, aaa, bbbb, null, null, null
<==        Row: 666, hibernate, 持久层框架, null, null, null
<==      Total: 13
13
Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
Returned connection 1199262943 to pool.
---------
Opening JDBC Connection
Checked out connection 1199262943 from pool.
Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
==>  Preparing: select * from t_user 
==> Parameters: 
<==    Columns: id, user_name, real_name, password, age, d_id
<==        Row: 1, zhangsan, 张三, 123456, 18, null
<==        Row: 2, lisi, 李四, 11111, 19, null
<==        Row: 3, wangwu, 王五, 111, 22, 1001
<==        Row: 4, wangwu, 王五, 111, 22, 1001
<==        Row: 5, wangwu, 王五, 111, 22, 1001
<==        Row: 6, wangwu, 王五, 111, 22, 1001
<==        Row: 7, wangwu, 王五, 111, 22, 1001
<==        Row: 8, aaa, bbbb, null, null, null
<==        Row: 9, aaa, bbbb, null, null, null
<==        Row: 10, aaa, bbbb, null, null, null
<==        Row: 11, aaa, bbbb, null, null, null
<==        Row: 12, aaa, bbbb, null, null, null
<==        Row: 666, hibernate, 持久层框架, null, null, null
<==      Total: 13
13
Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@477b4cdf]
Returned connection 1199262943 to pool.
```

通过输出我们能够发现，不同的Session中的相同操作，一级缓存是没有起作用的。

### 4.3 二级缓存

二级缓存是用来解决一级缓存不能跨会话共享的问题的，范围是namespace级别的，可以被多个SqlSession共享（只要是同一个接口里面的相同方法，都可以共享），生命周期和应用同步。

二级缓存的设置，首先是settings中的cacheEnabled要设置为true，当然默认的就是为true，这个步骤决定了在创建Executor对象的时候是否通过CachingExecutor来装饰。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/b74f186142cf4eaa81db6937693b731f.png)

那么设置了cacheEnabled标签为true是否就意味着 二级缓存是否一定可用呢？当然不是，我们还需要在 对应的映射文件中添加 cache 标签才行。

```xml
<!-- 声明这个namespace使用二级缓存 -->
<cache type="org.apache.ibatis.cache.impl.PerpetualCache"
      size="1024"  <!—最多缓存对象个数，默认1024-->
      eviction="LRU" <!—回收策略-->
      flushInterval="120000" <!—自动刷新时间 ms，未配置时只有调用时刷新-->
      readOnly="false"/> <!—默认是false（安全），改为true可读写时，对象必须支持序列化 -->
```

cache属性详解：

| **属性** | **含义**           | **取值**                                                                                                                                                                                                                                         |
| -------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| type           | 缓存实现类               | 需要实现Cache接口，默认是PerpetualCache，可以使用第三方缓存                                                                                                                                                                                            |
| size           | 最多缓存对象个数         | 默认1024                                                                                                                                                                                                                                               |
| eviction       | 回收策略（缓存淘汰算法） | LRU  – 最近最少使用的：移除最长时间不被使用的对象（默认）。FIFO – 先进先出：按对象进入缓存的顺序来移除它们。SOFT – 软引用：移除基于垃圾回收器状态和软引用规则的对象。WEAK – 弱引用：更积极地移除基于垃圾收集器状态和弱引用规则的对象。             |
| flushInterval  | 定时自动清空缓存间隔     | 自动刷新时间，单位 ms，未配置时只有调用时刷新                                                                                                                                                                                                          |
| readOnly       | 是否只读                 | true：只读缓存；会给所有调用者返回缓存对象的相同实例。因此这些对象不能被修改。这提供了很重要的性能优势。false：读写缓存；会返回缓存对象的拷贝（通过序列化），不会共享。这会慢一些，但是安全，因此默认是 false。改为false可读写时，对象必须支持序列化。 |
| blocking       | 启用阻塞缓存             | 通过在get/put方式中加锁，保证只有一个线程操作缓存，基于Java重入锁实现                                                                                                                                                                                  |

再来看下cache标签在源码中的体现，创建cacheKey

```java
//创建数据库查询并支持一级缓存功能 
@Override
//MappedStatement ms：封装了 SQL 语句、参数映射等配置信息
//Object parameterObject：查询参数（可能是单个值或对象）
//RowBounds rowBounds：分页参数（起始位置、查询条数）
//ResultHandler resultHandler：结果处理器（用于自定义结果处理逻辑）
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    // 获取SQL
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    // 创建CacheKey：什么样的SQL是同一条SQL？ >>
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }
```

createCacheKey自行进去查看

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/b9d7efec185f4c4bb8c58039c7fc9469.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/401bb61700eb4ab1ad394291b4d9ea9c.png)

而这看到的和我们前面在缓存初始化时看到的 cache 标签解析操作是对应上的。所以我们要开启二级缓存两个条件都要满足。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/03844c7d37234faaadfd7bf72e3836ce.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/38ca26ff36294fdc831c6b123dd8b394.png)

这样的设置表示当前的映射文件中的相关查询操作都会触发二级缓存，但如果某些个别方法我们不希望走二级缓存怎么办呢？我们可以在标签中添加一个 useCache=false 来实现的设置不使用二级缓存

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/c4a612567d7c4325b6c9640e962e985b.png)

还有就是当我们执行的对应的DML操作，在MyBatis中会清空对应的二级缓存和一级缓存。

```java
  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    // 增删改查的标签上有属性：flushCache="true" （select语句默认是false）
    // 一级二级缓存都会被清理
    if (cache != null && ms.isFlushCacheRequired()) {
      tcm.clear(cache);
    }
  }
```

在解析映射文件的时候DML操作flushCacheRequired为true

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1658467051071/10478c01e1b64fefbaf40f9d202656c7.png)

### 4.4 第三方缓存

```
在实际开发的时候我们一般也很少使用MyBatis自带的二级缓存，这时我们会使用第三方的缓存工具Ehcache获取Redis来实现,那么他们是如何来实现的呢？
```

https://github.com/mybatis/redis-cache

添加依赖

```xml
<dependency>
   <groupId>org.mybatis.caches</groupId>
   <artifactId>mybatis-redis</artifactId>
   <version>1.0.0-beta2</version>
</dependency>
```

然后加上Cache标签的配置

```xml
  <cache type="org.mybatis.caches.redis.RedisCache"
         eviction="FIFO" 
         flushInterval="60000" 
         size="512" 
         readOnly="true"/>
```

然后添加redis的属性文件

```properties
host=192.168.100.120
port=6379
connectionTimeout=5000
soTimeout=5000
database=0
```



# 二、日志模块

首先日志在我们开发过程中占据了一个非常重要的地位，是开发和运维管理之间的桥梁，在Java中的日志框架也非常多，Log4j,Log4j2,Apache Commons Log,java.util.logging,slf4j等，这些工具对外的接口也都不尽相同，为了统一这些工具，MyBatis定义了一套统一的日志接口供上层使用。首先大家对于适配器模式要了解下哦。

## 1、Log

```
Log接口中定义了四种日志级别，相比较其他的日志框架的多种日志级别显得非常的精简，但也能够满足大多数常见的使用了
```

```java
public interface Log {

  boolean isDebugEnabled();

  boolean isTraceEnabled();

  void error(String s, Throwable e);//包含了错误信息和异常对象

  void error(String s);

  void debug(String s);//输出调试级别日志

  void trace(String s);

  void warn(String s);

}
```

## 2、LogFactory

```
LogFactory工厂类负责创建日志组件适配器，
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/933b55b3087a4bf39eb4976f9345bc8a.png)

```
在LogFactory类加载时会执行其静态代码块，其逻辑是按序加载并实例化对应日志组件的适配器，然后使用LogFactory.logConstructor这个静态字段，记录当前使用的第三方日志组件的适配器。具体代码如下，每个方法都比较简单就不一一赘述了。
```

## 3、 日志应用

```
那么在MyBatis系统启动的时候日志框架是如何选择的呢？首先我们在全局配置文件中我们可以设置对应的日志类型选择
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/40d96814f56d4a06998ec7a86e8310fb.png)

这个"STDOUT_LOGGING"是怎么来的呢？在Configuration的构造方法中其实是设置的各个日志实现的别名的

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/9149237a3e8f4b1180f8a11f891782eb.png)

然后在解析全局配置文件的时候就会处理日志的设置

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/92ad10bccca74a3a9917a71b72a28762.png)

进入方法

```java
  private void loadCustomLogImpl(Properties props) {//存储配置信息
    // 获取 logImpl设置的 日志 类型
    Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
    // 设置日志
    configuration.setLogImpl(logImpl);
  }
```

进入setLogImpl方法中

```java
  public void setLogImpl(Class<? extends Log> logImpl) {
    if (logImpl != null) {
      this.logImpl = logImpl; // 记录日志的类型
      // 设置 适配选择
      LogFactory.useCustomLogging(this.logImpl);//将日志实现类应用到日志工厂
    }
  }
```

再进入useCustomLogging方法

```java
  public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
    setImplementation(clazz);//设置自定义的日志实现类
  }
```

再进入

```java
//通过指定的日志类来初始化日志系统 
private static void setImplementation(Class<? extends Log> implClass) {
    try {
      // 获取指定适配器的构造方法
      Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
      // 实例化适配器
      Log log = candidate.newInstance(LogFactory.class.getName());
        //日志如果处于调试模式，则输出初始化信息
      if (log.isDebugEnabled()) {
        log.debug("Logging initialized using '" + implClass + "' adapter.");
      }
      // 初始化 logConstructor 字段
      logConstructor = candidate;
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }
```

这就关联上了我们前面在LogFactory中看到的代码，启动测试方法看到的日志也和源码中的对应上来了，还有就是我们自己设置的会覆盖掉默认的sl4j日志框架的配置

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/87b82d550da04f58a0f7f23c8bfc072a.png)

## 4、JDBC 日志

```
当我们开启了 STDOUT的日志管理后，当我们执行SQL操作时我们发现在控制台中可以打印出相关的日志信息
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/f893d8c73dc045e1ba4ae4fdb3f14703.png)

```
那这些日志信息是怎么打印出来的呢？原来在MyBatis中的日志模块中包含了一个jdbc包，它并不是将日志信息通过jdbc操作保存到数据库中，而是通过JDK动态代理的方式，将JDBC操作通过指定的日志框架打印出来。下面我们就来看看它是如何实现的。
```

### 4.1 BaseJdbcLogger

```
BaseJdbcLogger是一个抽象类，它是jdbc包下其他Logger的父类。继承关系如下
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/4a73985d6e9e412e94dc8a9f76ce1081.png)

```
从图中我们也可以看到4个实现都实现了InvocationHandler接口。属性含义如下
```

```java
  // 记录 PreparedStatement 接口中定义的常用的set*() 方法
  protected static final Set<String> SET_METHODS;
  // 记录了 Statement 接口和 PreparedStatement 接口中与执行SQL语句有关的方法
  protected static final Set<String> EXECUTE_METHODS = new HashSet<>();

  // 记录了PreparedStatement.set*() 方法设置的键值对
  private final Map<Object, Object> columnMap = new HashMap<>();
  // 记录了PreparedStatement.set*() 方法设置的键 key
  private final List<Object> columnNames = new ArrayList<>();
  // 记录了PreparedStatement.set*() 方法设置的值 Value
  private final List<Object> columnValues = new ArrayList<>();

  protected final Log statementLog;// 用于日志输出的Log对象
  protected final int queryStack;  // 记录了SQL的层数，用于格式化输出SQL
```

```
其他几个方法可自行观看
```

### 4.2 ConnectionLogger

```
ConnectionLogger的作用是记录数据库连接相关的日志信息，在实现中是创建了一个Connection的代理对象，在每次Connection操作的前后我们都可以实现日志的操作。
```

```java
//作为动态代理的调用处理器
public final class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

  // 真正的Connection对象
  private final Connection connection;
//构造私有方法，接受真实连接、日志对象和查询堆栈参数
  private ConnectionLogger(Connection conn, Log statementLog, int queryStack) {
    super(statementLog, queryStack);
    this.connection = conn;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] params)
      throws Throwable {
    try {
      // 如果是调用从Object继承过来的方法，就直接调用 toString,hashCode,equals等
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, params);
      }
      // 如果调用的是 prepareStatement方法
      if ("prepareStatement".equals(method.getName())) {
        if (isDebugEnabled()) {
          debug(" Preparing: " + removeBreakingWhitespace((String) params[0]), true);
        }
        // 创建  PreparedStatement
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        // 然后创建 PreparedStatement 的代理对象 增强
        stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
        return stmt;
        // 同上
      } else if ("prepareCall".equals(method.getName())) {
        if (isDebugEnabled()) {
          debug(" Preparing: " + removeBreakingWhitespace((String) params[0]), true);
        }
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        stmt = PreparedStatementLogger.newInstance(stmt, statementLog, queryStack);
        return stmt;
        // 同上
      } else if ("createStatement".equals(method.getName())) {
        Statement stmt = (Statement) method.invoke(connection, params);
        stmt = StatementLogger.newInstance(stmt, statementLog, queryStack);
        return stmt;
      } else {
        return method.invoke(connection, params);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  /**
   * Creates a logging version of a connection.
   *
   * @param conn - the original connection
   * @return - the connection with logging
   */
  public static Connection newInstance(Connection conn, Log statementLog, int queryStack) {
    InvocationHandler handler = new ConnectionLogger(conn, statementLog, queryStack);
    ClassLoader cl = Connection.class.getClassLoader();
    // 创建了 Connection的 代理对象 目的是 增强 Connection对象 给他添加了日志功能
    return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
  }

  /**
   * return the wrapped connection.
   *
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

}
```

其他几个xxxxLogger的实现和ConnectionLogger几乎是一样的就不在次赘述了，请自行观看。

### 4.3 应用实现

```
在实际处理的时候，日志模块是如何工作的，我们来看看。
```

在我们要执行SQL语句前需要获取Statement对象，而Statement对象是通过Connection获取的，所以我们在SimpleExecutor中就可以看到相关的代码

```java
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    Connection connection = getConnection(statementLog);//首先获取数据库连接对象
    // 获取 Statement 对象
    stmt = handler.prepare(connection, transaction.getTimeout());
    // 为 Statement 设置参数
    handler.parameterize(stmt);
    return stmt;
  }
```

先进入如到getConnection方法中

```java
  protected Connection getConnection(Log statementLog) throws SQLException {
    Connection connection = transaction.getConnection();//先从事务中获取数据库的连接
    if (statementLog.isDebugEnabled()) {//判断是否开启了DEBUG几倍日志
      // 创建Connection的日志代理对象
      return ConnectionLogger.newInstance(connection, statementLog, queryStack);
    } else {
      return connection;
    }
  }
```

在进入到handler.prepare方法中

```java
  @Override
  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
      String[] keyColumnNames = mappedStatement.getKeyColumns();
      if (keyColumnNames == null) {
        return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      } else {
        // 在执行 prepareStatement 方法的时候会进入进入到ConnectionLogger的invoker方法中
        return connection.prepareStatement(sql, keyColumnNames);
      }
    } else if (mappedStatement.getResultSetType() == ResultSetType.DEFAULT) {
      return connection.prepareStatement(sql);
    } else {
      return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    }
  }
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1659100133022/7855bc6237014c62b19db8374f66a729.png)

在执行sql语句的时候

```java
  @Override
  public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    PreparedStatement ps = (PreparedStatement) statement;
    // 到了JDBC的流程
    ps.execute(); // 本质上 ps 也是 日志代理对象
    // 处理结果集
    return resultSetHandler.handleResultSets(ps);
  }
```

如果是查询操作，后面的ResultSet结果集操作，其他是也通过ResultSetLogger来处理的，前面的清楚了，后面的就很容易的。



# 三、DataSource

&emsp;&emsp;首先大家要清楚DataSource属于MyBatis三层架构设计的基础层
![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/9b8b4cd4f4ba42c2acd6386e9df7b989.png)

&emsp;&emsp;然后我们来看看具体的实现。
&emsp;&emsp;在数据持久层中，数据源是一个非常重要的组件，其性能直接关系到整个数据持久层的性能，在实际开发中我们常用的数据源有 Apache Common DBCP，C3P0，Druid 等，MyBatis不仅可以集成第三方数据源，还提供的有自己实现的数据源。

&emsp;&emsp; 在MyBatis中提供了两个 javax.sql.DataSource 接口的实现，分别是 PooledDataSource 和 UnpooledDataSource .

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/c0379eb993044948aa7474297d7ba5e6.png)

## 1 DataSourceFactory

&emsp;&emsp; DataSourceFactory是用来创建DataSource对象的，接口中声明了两个方法，作用如下

```java
public interface DataSourceFactory {
  // 设置 DataSource 的相关属性，一般紧跟在初始化完成之后
  void setProperties(Properties props);

  // 获取 DataSource 对象
  DataSource getDataSource();

}
```

&emsp;&emsp;DataSourceFactory接口的两个具体实现是 UnpooledDataSourceFactory 和 PooledDataSourceFactory 这两个工厂对象的作用通过名称我们也能发现是用来创建不带连接池的数据源对象和创建带连接池的数据源对象，先来看下 UnpooledDataSourceFactory  中的方法

```java
  /**
   * 完成对 UnpooledDataSource 的配置
   * @param properties 封装的有 DataSource 所需要的相关属性信息
   */
  @Override
  public void setProperties(Properties properties) {
    Properties driverProperties = new Properties();
    // 创建 DataSource 对应的 MetaObject 对象
    MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);
    // 遍历 Properties 集合，该集合中配置了数据源需要的信息
    for (Object key : properties.keySet()) {
      String propertyName = (String) key; // 获取属性名称
      if (propertyName.startsWith(DRIVER_PROPERTY_PREFIX)) {
        // 以 "driver." 开头的配置项是对 DataSource 的配置
        String value = properties.getProperty(propertyName);
        driverProperties.setProperty(propertyName.substring(DRIVER_PROPERTY_PREFIX_LENGTH), value);
      } else if (metaDataSource.hasSetter(propertyName)) {
        // 有该属性的 setter 方法
        String value = (String) properties.get(propertyName);
        Object convertedValue = convertValue(metaDataSource, propertyName, value);
        // 设置 DataSource 的相关属性值
        metaDataSource.setValue(propertyName, convertedValue);
      } else {
        throw new DataSourceException("Unknown DataSource property: " + propertyName);
      }
    }
    if (driverProperties.size() > 0) {
      // 设置 DataSource.driverProperties 的属性值
      metaDataSource.setValue("driverProperties", driverProperties);
    }
  }
```

```
UnpooledDataSourceFactory的getDataSource方法实现比较简单，直接返回DataSource属性记录的 UnpooledDataSource 对象
```


## 2 UnpooledDataSource

&emsp;&emsp;UnpooledDataSource 是 DataSource接口的其中一个实现，但是 UnpooledDataSource 并没有提供数据库连接池的支持，我们来看下他的具体实现吧

&emsp;&emsp;声明的相关属性信息

```java
  private ClassLoader driverClassLoader; // 加载Driver的类加载器
  private Properties driverProperties; // 数据库连接驱动的相关信息
  // 缓存所有已注册的数据库连接驱动
  private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

  private String driver; // 驱动
  private String url; // 数据库 url
  private String username; // 账号
  private String password; // 密码

  private Boolean autoCommit; // 是否自动提交
  private Integer defaultTransactionIsolationLevel; // 事务隔离级别
  private Integer defaultNetworkTimeout;
```

```
然后在静态代码块中完成了 Driver的复制
```


```java
  static {
    // 从 DriverManager 中获取 Drivers
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      // 将获取的 Driver 记录到 Map 集合中
      registeredDrivers.put(driver.getClass().getName(), driver);
    }
  }
```

```
UnpooledDataSource 中获取Connection的方法最终都会调用 doGetConnection() 方法。
```


```java
  private Connection doGetConnection(Properties properties) throws SQLException {
      // 初始化数据库驱动
      initializeDriver();
    // 创建真正的数据库连接
    Connection connection = DriverManager.getConnection(url, properties);
    // 配置Connection的自动提交和事务隔离级别
    configureConnection(connection);
    return connection;
  }
```

## 3  PooledDataSource

&emsp;&emsp;有开发经验的小伙伴都知道，在操作数据库的时候数据库连接的创建过程是非常耗时的，数据库能够建立的连接数量也是非常有限的，所以数据库连接池的使用是非常重要的，使用数据库连接池会给我们带来很多好处，比如可以实现数据库连接的重用，提高响应速度，防止数据库连接过多造成数据库假死，避免数据库连接泄漏等等。

首先来看下声明的相关的属性

```java
  // 管理状态
  private final PoolState state = new PoolState(this);

  // 记录UnpooledDataSource，用于生成真实的数据库连接对象
  private final UnpooledDataSource dataSource;

  // OPTIONAL CONFIGURATION FIELDS
  protected int poolMaximumActiveConnections = 10; // 最大活跃连接数
  protected int poolMaximumIdleConnections = 5; // 最大空闲连接数
  protected int poolMaximumCheckoutTime = 20000; // 最大checkout时间
  protected int poolTimeToWait = 20000; // 无法获取连接的线程需要等待的时长
  protected int poolMaximumLocalBadConnectionTolerance = 3; //
  protected String poolPingQuery = "NO PING QUERY SET"; // 测试的SQL语句
  protected boolean poolPingEnabled; // 是否允许发送测试SQL语句
  // 当连接超过 poolPingConnectionsNotUsedFor毫秒未使用时，会发送一次测试SQL语句，检测连接是否正常
  protected int poolPingConnectionsNotUsedFor;
 // 根据数据库URL，用户名和密码生成的一个hash值。
  private int expectedConnectionTypeCode;
```

&emsp;&emsp;然后重点来看下 getConnection 方法，该方法是用来给调用者提供 Connection 对象的。

```java
  @Override
  public Connection getConnection() throws SQLException {
    return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
  }
```

&emsp;&emsp;我们会发现其中调用了 popConnection 方法，在该方法中 返回的是 PooledConnection 对象，而 PooledConnection 对象实现了 InvocationHandler 接口，所以会使用到Java的动态代理，其中相关的属性为

```java
  private static final String CLOSE = "close";
  private static final Class<?>[] IFACES = new Class<?>[] { Connection.class };

  private final int hashCode;
  private final PooledDataSource dataSource;
  //  真正的数据库连接
  private final Connection realConnection;
  //  数据库连接的代理对象
  private final Connection proxyConnection;
  private long checkoutTimestamp; // 从连接池中取出该连接的时间戳
  private long createdTimestamp; // 该连接创建的时间戳
  private long lastUsedTimestamp; // 最后一次被使用的时间戳
  private int connectionTypeCode; // 又数据库URL、用户名和密码计算出来的hash值，可用于标识该连接所在的连接池
  // 连接是否有效的标志
  private boolean valid;
```

&emsp;&emsp;重点关注下invoke 方法

```java
//拦截对代理对象的方法调用  
@Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    if (CLOSE.equals(methodName)) {
      // 如果是 close 方法被执行则将连接放回连接池中，而不是真正的关闭数据库连接
      dataSource.pushConnection(this);
      return null;
    }
    try {
      if (!Object.class.equals(method.getDeclaringClass())) {
        // issue #579 toString() should never fail
        // throw an SQLException instead of a Runtime
        // 通过上面的 valid 字段来检测 连接是否有效
        checkConnection();
      }
      // 调用真正数据库连接对象的对应方法
      return method.invoke(realConnection, args);//将方法转发给真实的数据库连接对象
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }

  }
```

&emsp;&emsp;还有就是前面提到的 PoolState 对象，它主要是用来管理 PooledConnection 对象状态的组件，通过两个 ArrayList 集合分别管理空闲状态的连接和活跃状态的连接，定义如下：

```java
  protected PooledDataSource dataSource;
  // 空闲的连接
  protected final List<PooledConnection> idleConnections = new ArrayList<>();
  // 活跃的连接
  protected final List<PooledConnection> activeConnections = new ArrayList<>();
  protected long requestCount = 0; // 请求数据库连接的次数
  protected long accumulatedRequestTime = 0; // 获取连接累计的时间
  // CheckoutTime 表示应用从连接池中取出来，到归还连接的时长
  // accumulatedCheckoutTime 记录了所有连接累计的CheckoutTime时长
  protected long accumulatedCheckoutTime = 0;
  // 当连接长时间没有归还连接时，会被认为该连接超时
  // claimedOverdueConnectionCount 记录连接超时的个数
  protected long claimedOverdueConnectionCount = 0;
  // 累计超时时间
  protected long accumulatedCheckoutTimeOfOverdueConnections = 0;
  // 累计等待时间
  protected long accumulatedWaitTime = 0;
  // 等待次数
  protected long hadToWaitCount = 0;
  // 无效连接数
  protected long badConnectionCount = 0;
```

&emsp;&emsp;再回到 popConnection 方法中来看

```java
  private PooledConnection popConnection(String username, String password) throws SQLException {
    boolean countedWait = false;
    PooledConnection conn = null;
    long t = System.currentTimeMillis();
    int localBadConnectionCount = 0;

    while (conn == null) {
      synchronized (state) { // 确保多线程环境下连接池操作的线程安全
        if (!state.idleConnections.isEmpty()) { // 检测空闲连接
          // Pool has available connection 连接池中有空闲的连接
          conn = state.idleConnections.remove(0); // 获取连接
          if (log.isDebugEnabled()) {
            log.debug("Checked out connection " + conn.getRealHashCode() + " from pool.");
          }
        } else {// 当前连接池 没有空闲连接
          // Pool does not have available connection
          if (state.activeConnections.size() < poolMaximumActiveConnections) { // 活跃数没有达到最大连接数 可以创建新的连接
            // Can create new connection 创建新的数据库连接
            conn = new PooledConnection(dataSource.getConnection(), this);
            if (log.isDebugEnabled()) {
              log.debug("Created connection " + conn.getRealHashCode() + ".");
            }
          } else { // 活跃数已经达到了最大数 不能创建新的连接
            // Cannot create new connection 获取最先创建的活跃连接
            PooledConnection oldestActiveConnection = state.activeConnections.get(0);
            // 获取该连接的超时时间
            long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
            // 检查是否超时
            if (longestCheckoutTime > poolMaximumCheckoutTime) {
              // Can claim overdue connection  对超时连接的信息进行统计
              state.claimedOverdueConnectionCount++;
              state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
              state.accumulatedCheckoutTime += longestCheckoutTime;
              // 将超时连接移除 activeConnections
              state.activeConnections.remove(oldestActiveConnection);
              if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                // 如果超时连接没有提交 则自动回滚
                try {
                  oldestActiveConnection.getRealConnection().rollback();
                } catch (SQLException e) {
                  /*
                     Just log a message for debug and continue to execute the following
                     statement like nothing happened.
                     Wrap the bad connection with a new PooledConnection, this will help
                     to not interrupt current executing thread and give current thread a
                     chance to join the next competition for another valid/good database
                     connection. At the end of this loop, bad {@link @conn} will be set as null.
                   */
                  log.debug("Bad connection. Could not roll back");
                }
              }
              // 创建 PooledConnection，但是数据库中的真正连接并没有创建
              conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
              conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
              conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
              // 将超时的 PooledConnection 设置为无效
              oldestActiveConnection.invalidate();
              if (log.isDebugEnabled()) {
                log.debug("Claimed overdue connection " + conn.getRealHashCode() + ".");
              }
            } else {
              // Must wait  无空闲连接，无法创建新连接和无超时连接 那就只能等待
              try {
                if (!countedWait) {
                  state.hadToWaitCount++; // 统计等待次数
                  countedWait = true;
                }
                if (log.isDebugEnabled()) {
                  log.debug("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                }
                long wt = System.currentTimeMillis();
                state.wait(poolTimeToWait); // 阻塞等待
                // 统计累计的等待时间
                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
              } catch (InterruptedException e) {
                break;
              }
            }
          }
        }
        if (conn != null) {
          // ping to server and check the connection is valid or not
          // 检查 PooledConnection 是否有效
          if (conn.isValid()) {
            if (!conn.getRealConnection().getAutoCommit()) {
              conn.getRealConnection().rollback();
            }
            // 配置 PooledConnection 的相关属性
            conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
            conn.setCheckoutTimestamp(System.currentTimeMillis());
            conn.setLastUsedTimestamp(System.currentTimeMillis());
            state.activeConnections.add(conn);
            state.requestCount++; // 进行相关的统计
            state.accumulatedRequestTime += System.currentTimeMillis() - t;
          } else {
            if (log.isDebugEnabled()) {
              log.debug("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
            }
            state.badConnectionCount++;
            localBadConnectionCount++;
            conn = null;
            if (localBadConnectionCount > (poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance)) {
              if (log.isDebugEnabled()) {
                log.debug("PooledDataSource: Could not get a good connection to the database.");
              }
              throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
            }
          }
        }
      }

    }
```

&emsp;&emsp;为了更好的理解代码的含义，我们绘制了对应的流程图
![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/1a18b47fdcee4a859e54a6afa43bfbb9.png)

&emsp;&emsp;然后我们来看下当我们从连接池中使用完成了数据库的相关操作后，是如何来关闭连接的呢？通过前面的 invoke 方法的介绍其实我们能够发现，当我们执行代理对象的 close 方法的时候其实是执行的 pushConnection 方法。
![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/c7a3420588e5420f9d01634100d75015.png)

&emsp;&emsp;具体的实现代码为

```java
  protected void pushConnection(PooledConnection conn) throws SQLException {

    synchronized (state) {
      // 从 activeConnections 中移除 PooledConnection 对象
      state.activeConnections.remove(conn);
      if (conn.isValid()) { // 检测 连接是否有效
        if (state.idleConnections.size() < poolMaximumIdleConnections // 是否达到上限
            && conn.getConnectionTypeCode() == expectedConnectionTypeCode // 该 PooledConnection 是否为该连接池的连接
        ) {
          state.accumulatedCheckoutTime += conn.getCheckoutTime(); // 累计 checkout 时长
          if (!conn.getRealConnection().getAutoCommit()) { // 回滚未提交的事务
            conn.getRealConnection().rollback();
          }
          // 为返还连接创建新的 PooledConnection 对象
          PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
          // 添加到 空闲连接集合中
          state.idleConnections.add(newConn);
          newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
          newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
          conn.invalidate(); // 将原来的 PooledConnection 连接设置为无效
          if (log.isDebugEnabled()) {
            log.debug("Returned connection " + newConn.getRealHashCode() + " to pool.");
          }
          // 唤醒阻塞等待的线程
          state.notifyAll();
        } else { // 空闲连接达到上限或者 PooledConnection不属于当前的连接池
          state.accumulatedCheckoutTime += conn.getCheckoutTime(); // 累计 checkout 时长
          if (!conn.getRealConnection().getAutoCommit()) {
            conn.getRealConnection().rollback();
          }
          conn.getRealConnection().close(); // 关闭真正的数据库连接
          if (log.isDebugEnabled()) {
            log.debug("Closed connection " + conn.getRealHashCode() + ".");
          }
          conn.invalidate(); // 设置 PooledConnection 无线
        }
      } else {
        if (log.isDebugEnabled()) {
          log.debug("A bad connection (" + conn.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
        }
        state.badConnectionCount++; // 统计无效的 PooledConnection 对象个数
      }
    }
  }
```

&emsp;&emsp;为了便于理解，我们同样的来绘制对应的流程图：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/31d22f02010d413d8e97a2dfdbc5243e.png)

&emsp;&emsp;还有就是我们在源码中多处有看到 conn.isValid方法来检测连接是否有效

```java
  public boolean isValid() {
    return valid && realConnection != null && dataSource.pingConnection(this);
  }
```

&emsp;&emsp;dataSource.pingConnection(this)中会真正的实现数据库的SQL执行操作

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/8663452163c548a5b15f925221c9f786.png)

&emsp;&emsp;最后一点要注意的是在我们修改了任意的PooledDataSource中的属性的时候都会执行forceCloseAll来强制关闭所有的连接。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/1462/1702534164044/c92b3e697af54a4e958d05e672ee06d4.png)

```java
  /**
   * Closes all active and idle connections in the pool.
   */
  public void forceCloseAll() {
    synchronized (state) {
      // 更新 当前的 连接池 标识
      expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
      for (int i = state.activeConnections.size(); i > 0; i--) {// 处理全部的活跃连接
        try {
          // 获取 获取的连接
          PooledConnection conn = state.activeConnections.remove(i - 1);
          conn.invalidate(); // 标识为无效连接
          // 获取真实的 数据库连接
          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback(); // 回滚未处理的事务
          }
          realConn.close(); // 关闭真正的数据库连接
        } catch (Exception e) {
          // ignore
        }
      }
      // 同样的逻辑处理空闲的连接
      for (int i = state.idleConnections.size(); i > 0; i--) {
        try {
          PooledConnection conn = state.idleConnections.remove(i - 1);
          conn.invalidate();

          Connection realConn = conn.getRealConnection();
          if (!realConn.getAutoCommit()) {
            realConn.rollback();
          }
          realConn.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("PooledDataSource forcefully closed/removed all connections.");
    }
  }
```
