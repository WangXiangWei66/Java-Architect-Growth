## 1、Redis中高级数据类型的面试题

1、目前有10亿数量的自然数，乱序排列，需要对其排序。限制条件-在32位机器上面完成，内存限制为 2G。如何完成？
2、如何快速在亿级黑名单中快速定位URL地址是否在黑名单中？(每条URL平均64字节)
3、需要进行用户登陆行为分析，来确定用户的活跃情况？
4、某视频网站需要统计每天独立访客（UV）数量，每天有数亿次的访问，并且需要实时统计。如果使用精确计数，内存消耗巨大。如何用较小内存实现近似统计？
5、一个外卖平台需要实时查询用户周边3公里内的所有配送员位置（假设有百万级配送员在线），并快速返回距离用户最近的10个配送员。如何设计？

## 2、Redis的高级数据结构介绍

### Bitmap

Redis中的bitmap数据类型是一种特殊的数据结构，用于处理位操作。它是一个由二进制位组成的数组，每个位可以表示一个布尔值（0或1）。

Bitmap数据类型在Redis中使用字符串来表示，每个字符都可以存储8个位。这意味着一个长度为n的bitmap数据类型将占用n/8个字节的存储空间。

#### 操作命令

```
SETBIT key offset value：将指定偏移量上的位设置为指定的值（0或1）。 
GETBIT key offset：获取指定偏移量上的位的值。 
BITCOUNT key [start end]：统计指定范围内的位中值为1的个数。 
BITOP operation destkey key [key ...]：对一个或多个bitmap进行位操作，并将结果存储在目标bitmap中。支持的位操作包括AND、OR、XOR、NOT等。 
```

#### 使用Bitmap解决的问题

##### **面试题1:** 目前有10亿数量的自然数，乱序排列，需要对其排序。限制条件-在32位机器上面完成，内存限制为 2G。如何完成？

由于是自然数（假设为非负整数），且32位机器上自然数范围是0到约42亿（2^32），是少于10亿个数。我们可以使用Bitmap（位图）来排序。

- 每个数用一个比特表示，存在则为1，否则为0。
- 需要的内存：2^32比特 ≈ 4,294,967,296 bit ≈ 536,870,912 字节 ≈ 536 MB（远小于2G）。
- 步骤：

1. 初始化一个长度为2^32的位图（全0）。
2. 遍历所有数字，将对应位设置为1。
3. 从小到大遍历位图，输出位为1的索引（即排序后的数）。###### 代码案例

com.msb.caffeine.lock.redistypes.BitmapSortingTest

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/c656282d174444a9b248a1cdbe7a11eb.png)

##### **面试题3:** 需要进行用户登陆行为分析，来确定用户的活跃情况？

- 如果分析的是每日活跃用户数（DAU）等，可以使用Bitmap：

每个用户分配一个唯一ID（整数），每一天用一个Bitmap，用户登录则将该用户ID对应的位设为1。

统计某一天的活跃用户数：计算该天Bitmap中1的个数（BITCOUNT）。

统计连续多天活跃用户：对多个Bitmap进行AND操作。

com.msb.caffeine.lock.redistypes.UserActivityTest

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/7aaf239aff1843158fdaad4c48921c9a.png)

### 布隆过滤器

#### 布隆过滤器简介

**1970 年布隆提出了一种布隆过滤器的算法，用来判断一个元素是否在一个集合中。这种算法由一个二进制数组和一个 Hash 算法组成。**

实际上，布隆过滤器广泛应用于网页黑名单系统、垃圾邮件过滤系统、爬虫网址判重系统等，Google 著名的分布式数据库 Bigtable 使用了布隆过滤器来查找不存在的行或列，以减少磁盘查找的IO次数，Google Chrome浏览器使用了布隆过滤器加速安全浏览服务。

#### 布隆过滤器解决缓存穿透问题

**缓存穿透**

是指查询一个根本不存在的数据，缓存层和存储层都不会命中，于是这个请求就可以随意访问数据库，这个就是缓存穿透，缓存穿透将导致不存在的数据每次请求都要到存储层去查询，失去了缓存保护后端存储的意义。

缓存穿透问题可能会使后端存储负载加大，由于很多后端存储不具备高并发性，甚至可能造成后端存储宕掉。通常可以在程序中分别统计总调用数、缓存层命中数、存储层命中数，如果发现大量存储层空命中，可能就是出现了缓存穿透问题。

造成缓存穿透的基本原因有两个。

第一，自身业务代码或者数据出现问题，比如，我们数据库的 id 都是1开始自增上去的，如发起为id值为 -1 的数据或 id 为特别大不存在的数据。如果不对参数做校验，数据库id都是大于0的，我一直用小于0的参数去请求你，每次都能绕开Redis直接打到数据库，数据库也查不到，每次都这样，并发高点就容易崩掉了。

第二,一些恶意攻击、爬虫等造成大量空命中。下面我们来看一下如何解决缓存穿透问题。

**1.缓存空对象**

当存储层不命中，到数据库查发现也没有命中，那么仍然将空对象保留到缓存层中，之后再访问这个数据将会从缓存中获取,这样就保护了后端数据源。

缓存空对象会有两个问题:

第一，空值做了缓存，意味着缓存层中存了更多的键，需要更多的内存空间(如果是攻击，问题更严重),比较有效的方法是针对这类数据设置一个较短的过期时间，让其自动剔除。

第二，缓存层和存储层的数据会有一段时间窗口的不一致，可能会对业务有一定影响。例如过期时间设置为5分钟，如果此时存储层添加了这个数据，那此段时间就会出现缓存层和存储层数据的不一致，此时可以利用消前面所说的数据一致性方案处理。

**2.布隆过滤器拦截**

在访问缓存层和存储层之前,将存在的key用布隆过滤器提前保存起来,做第一层拦截。例如:一个推荐系统有4亿个用户id，每个小时算法工程师会根据每个用户之前历史行为计算出推荐数据放到存储层中,但是最新的用户由于没有历史行为,就会发生缓存穿透的行为,为此可以将所有推荐数据的用户做成布隆过滤器。如果布隆过滤器认为该用户id不存在,那么就不会访问存储层,在一定程度保护了存储层。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1667197328004/91ac8664908a42a1bf9a1ce23aeec6c8.png)

这种方法适用于数据命中不高、数据相对固定、实时性低(通常是数据集较大)的应用场景,代码维护较为复杂,但是缓存空间占用少。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/801f60ff2d28436faaaab3007dd7d893.png)

##### 面试2、如何快速在亿级黑名单中快速定位URL地址是否在黑名单中？（每条URL平均64字节）

**期望答案（使用布隆过滤器）**：

由于URL数量巨大（亿级），且每个URL较大（64字节），直接存储需要大量内存（10亿*64字节≈64GB），内存不够。

使用布隆过滤器（Bloom Filter）来解决：

- 布隆过滤器是一个位数组（Bitmap）和多个哈希函数组成。
- 添加URL时，用多个哈希函数计算URL的多个哈希值，将位数组对应位置1。
- 检查URL时，同样计算多个哈希值，如果所有位都为1，则可能在黑名单中（存在误判）；如果任一为0，则一定不在。
- 优点：内存占用小（远小于64GB），查询速度快。
- 缺点：有误判率（但黑名单场景可以接受，因为误判只会将正常URL误认为黑名单，可以通过白名单二次校验）。

1. ###### 代码案例

com.msb.caffeine.lock.redistypes.BloomFilterTest

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/9e04bd71e55e41b281d40311836e79ac.png)

### HyperLogLog

#### 介绍

HyperLogLog并不是一种新的数据结构(实际类型为字符串类型)，而是一种基数算法,通过HyperLogLog可以利用极小的内存空间完成独立总数的统计（去重的），数据集可以是IP、Email、ID等。

本质就是UV统计：如果你负责开发维护一个大型的网站，有一天产品经理要网站每个网页每天的 UV 数据（每个页面有多少个不同的用户访问过），然后让你来开发这个统计模块，你会如何实现？

1、Redis的incr做计数器是不行的，因为这里有重复，重复的用户访问一次，这个count就加1是不行的。

2、使用set集合存储所有当天访问过此页面的用户 ID，当一个请求过来时，我们使用 sadd 将用户 ID 塞进去就可以了。通过 scard 可以取出这个集合的大小，这个数字就是这个页面的 UV 数据。

不过使用set集合，这就非常浪费空间。如果这样的页面很多，那所需要的存储空间是惊人的。

3、如果你需要的数据不需要太精确，那么可以使用HyperLogLog，Redis 提供了 HyperLogLog 数据结构就是用来解决这种统计问题的。

**HyperLogLog 提供不精确的去重计数方案，虽然不精确但是也不是非常不精确，Redis官方给出标准误差是0.81%，这样的精确度已经可以满足上面的UV 统计需求了。**

#### 操作命令

HyperLogLog提供了3个命令: pfadd、pfcount、pfmerge。

```
pfadd key element [element …] 向HyperLogLog 添加元素,如果添加成功返回1:
pfcount key [key …]   计算一个或多个HyperLogLog的独立总数
pfmerge destkey sourcekey [sourcekey ... ]  求出多个HyperLogLog的并集并赋值给destkey
```

#### 演示案例

```
pfadd uv-count u1 u2 u3 u4 u5 u6 u7 u8 u9
pfcount uv-count
pfadd uv-count2 u11 u12 u13 u14 u15 u16 u17 u18 u19
pfmerge uv-all uv-count uv-count2
pfcount uv-all
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/d3fcc9d9c181463baa99e922a7430d56.png)

#### 代码演示

```
package com.msb.redis.advtype;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/*HyperLogLog测试UV与set的对比*/
public class HyperLogLogTest {
    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 30000);
        Jedis jedis1 = null;
        try {
            jedis1 = jedisPool.getResource();
            for(int i=0;i<10000;i++){ //1万个元素
                jedis1.pfadd("hyper-count","user"+i);
            }
            long total = jedis1.pfcount("hyper-count");
            System.out.println("实际次数:" + 10000 + "，HyperLogLog统计次数:"+total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis1.close();
        }


        Jedis jedis2 = null;
        try {
            jedis2 = jedisPool.getResource();
            for(int i=0;i<10000;i++){ //1万个元素
                jedis2.sadd("set-count","user"+i);
            }
            long total = jedis2.scard("set-count");
            System.out.println("实际次数:" + 10000 + "，set统计次数:"+total);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis2.close();
        }

    }
}

```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/a3e368762e234d0daab65b557709713c.png)

同时使用debug命令测试一下他们的存储的比较，只有之前的1 /10

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/060c7a0bfc794cbc87fa8cc47f7264e1.png)

可以看到，HyperLogLog内存占用量小得惊人，但是用如此小空间来估算如此巨大的数据，必然不是100%的正确，其中一定存在误差率。前面说过，Redis官方给出的数字是0.81%的失误率。

##### 面试4、某视频网站需要统计每天独立访客（UV）数量，每天有数亿次的访问，并且需要实时统计。如果使用精确计数，内存消耗巨大。如何用较小内存实现近似统计？

1. ###### 代码案例

com.msb.caffeine.lock.redistypes.HyperLogLogTest

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/9fabdde6dcad4d9d9f3880bd4b39bbd4.png)

#### 底层原理

原理挺难的，总结一句话就是：HyperLogLog基于概率论中伯努利试验并结合了极大似然估算方法，并做了分桶优化。

##### 基本原理

HyperLogLog基于概率论中伯努利试验并结合了极大似然估算方法，并做了分桶优化。

实际上目前还没有发现更好的在大数据场景中准确计算基数的高效算法，因此在不追求绝对准确的情况下，使用概率算法算是一个不错的解决方案。概率算法不直接存储数据集合本身，通过一定的概率统计方法预估值，这种方法可以大大节省内存，同时保证误差控制在一定范围内。目前用于基数计数的概率算法包括:

举个例子来理解HyperLogLog
算法，有一天李瑾老师和马老师玩打赌的游戏。

规则如下: 抛硬币的游戏，每次抛的硬币可能正面，可能反面，没回合一直抛，直到每当抛到正面回合结束。

然后我跟马老师说，抛到正面最长的回合用到了7次，你来猜一猜，我用到了多少个回合做到的？

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/b908ff160cdd4eed858e76b248fd880e.png)

进行了n次实验，比如上图：

第一次试验: 抛了3次才出现正面，此时 k=3，n=1

第二次试验: 抛了2次才出现正面，此时 k=2，n=2

第三次试验: 抛了4次才出现正面，此时 k=4，n=3

…………

第n 次试验：抛了7次才出现正面，此时我们估算，k=7

马老师说大概你抛了128个回合。这个是怎么算的。

k是每回合抛到1所用的次数，我们已知的是最大的k值，可以用kmax表示。由于每次抛硬币的结果只有0和1两种情况，因此，能够推测出kmax在任意回合出现的概率 ，并由kmax结合极大似然估算的方法推测出n的次数n =
2^(k_max) 。概率学把这种问题叫做伯努利实验。

但是问题是，这种本身就是概率的问题，我跟马老师说，我只用到12次，并且有视频为证。

所以这种预估方法存在较大误差，为了改善误差情况，HLL中引入分桶平均的概念。

同样举抛硬币的例子，如果只有一组抛硬币实验，显然根据公式推导得到的实验次数的估计误差较大；如果100个组同时进行抛硬币实验，受运气影响的概率就很低了，每组分别进行多次抛硬币实验，并上报各自实验过程中抛到正面的抛掷次数的最大值，就能根据100组的平均值预估整体的实验次数了。

分桶平均的基本原理是将统计数据划分为m个桶，每个桶分别统计各自的kmax,并能得到各自的基数预估值，最终对这些基数预估值求平均得到整体的基数估计值。LLC中使用几何平均数预估整体的基数值，但是当统计数据量较小时误差较大；HLL在LLC基础上做了改进，**采用调和平均数过滤掉不健康的统计值**。

什么叫调和平均数呢？举个例子

求平均工资：A的是1000/月，B的30000/月。采用平均数的方式就是：
(1000 + 30000) / 2 = 15500

采用调和平均数的方式就是：
2/(1/1000 + 1/30000) ≈ 1935.484

可见调和平均数比平均数的好处就是不容易受到大的数值的影响，比平均数的效果是要更好的。

##### 结合Redis的实现理解原理

现在我们和前面的业务场景进行挂钩：统计网页每天的 UV 数据。

**1.转为比特串**

通过hash函数，将数据转为二进制的比特串，例如输入5，便转为：101。为什么要这样转化呢？

是因为要和抛硬币对应上，比特串中，0 代表了反面，1 代表了正面，如果一个数据最终被转化了 10010000，那么从右往左，从低位往高位看，我们可以认为，首次出现 1 的时候，就是正面。

那么基于上面的估算结论，我们可以通过多次抛硬币实验的最大抛到正面的次数来预估总共进行了多少次实验，同样也就可以根据存入数据中，转化后的出现了 1 的最大的位置 k_max 来估算存入了多少数据。

**2.分桶**

分桶就是分多少轮。抽象到计算机存储中去，就是存储的是一个以单位是比特(bit)，长度为 L 的大数组 S ，将 S 平均分为 m 组，注意这个 m 组，就是对应多少轮，然后每组所占有的比特个数是平均的，设为 P。容易得出下面的关系：

比如有4个桶的话，那么可以截取低2位作为分桶的依据。

比如

10010000   进入0号桶

10010001   进入1号桶

10010010   进入2号桶

10010011   进入3号桶

##### Redis 中的 HyperLogLog 实现

**pfadd**

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/116ef8fb48584cc5910184aaf50092a1.png)

当我们执行这个操作时，lijin这个字符串就会被转化成64个bit的二进制比特串。

0010....0001  64位

然后在Redis中要分到16384个桶中（为什么是这么多桶：第一降低误判，第二，用到了14位二进制：2的14次方=16384）

怎么分？根据得到的比特串的后14位来做判断即可。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/bd71287e85294b14b95e3fcb82243fab.png)

根据上述的规则，我们知道这个数据要分到 1号桶，同时从左往右（低位到高位）计算第1个出现的1的位置，这里是第4位，那么就往这个1号桶插入4的数据（转成二进制）

如果有第二个数据来了，按照上述的规则进行计算。

那么问题来了，如果分到桶的数据有重复了（这里比大小，大的替换小的）：

规则如下，比大小（比出现位置的大小），比如有个数据是最高位才出现1，那么这个位置算出来就是50，50比4大，则进行替换。1号桶的数据就变成了50（二进制是110010）

所以这里可以看到，每个桶的数据一般情况下6位存储即可。

所以我们这里可以推算一下一个key的HyperLogLog只占据多少的存储。

16384*6 /8/1024=12k。并且这里最多可以存储多少数据，因为是64位吗，所以就是2的64次方的数据，这个存储的数据非常非常大的，一般用户用long来定义，最大值也只有这么多。

**pfcount**

进行统计的时候，就是把16384桶，把每个桶的值拿出来，比如取出是 n,那么访问次数就是2的n次方。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/37ea459470614e8fad06c5eab8a009ca.png)

然后把每个桶的值做调和平均数，就可以算出一个算法值。

同时，在具体的算法实现上，HLL还有一个分阶段偏差修正算法。我们就不做更深入的了解了。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/50bed8f5a0394a93aa8033ee9f847672.png)

const和m都是Redis里面根据数据做的调和平均数。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/bab4508464bd4082ac671439d42bb8ad.png)

##### 代码演示

com.msb.caffeine.lock.redistypes.HyperLogLogSimulation

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/432cb4667a0048248657efa16682edde.png)

### GEO

Redis 3.2版本提供了GEO(地理信息定位)功能，支持存储地理位置信息用来实现诸如附近位置、摇一摇这类依赖于地理位置信息的功能。

在日常生活中，我们越来越依赖搜索“附近的餐馆”、在打车软件上叫车，这些都离不开基于位置信息服务（Location-Based Service，LBS）的应用。LBS 应用访问的数据是和人或物关联的一组经纬度信息，而且要能查询相邻的经纬度范围，GEO 就非常适合应用在LBS 服务的场景中。

#### 操作命令

```
geoadd key longitude latitude member [longitude latitude member ...  longitude、latitude、member分别是该地理位置的经度、纬度、成员
geopos key member [member ...] 获取地理位置信息
zrem key member 删除地理位置信息（GEO没有提供删除成员的命令）
geodist key member1 member2  [unit] 获取两个地理位置的距离 （unit代表返回结果的单位：m (meters)代表米。km (kilometers)代表公里。mi (miles)代表英里。ft(feet)代表尺。）

georadiusbymember key member radius m|km|ft|mi  [withcoord][withdist]  以一个地理位置为中心（成员）算出指定半径内（radius）的其他地理信息位置（其中radius  m | km |ft |mi是必需参数，指定了半径(带单位)）

georadius key longitude latitude radius m|km|ft|mi [withcoord][withdist]以一个地理位置为中心（latitude 经度、latitude 维度）算出指定半径内的其他地理信息位置（其中radius  m | km |ft |mi是必需参数，指定了半径(带单位)）

geohash key member [member ...]  获取geohash（将二维经纬度转换为一维字符串）

```

#### 演示案例

longitude、latitude、member分别是该地理位置的经度、纬度、成员，例如下面有5个城市的经纬度。

城市            经度             纬度             成员

北京            116.28          39.55            beijing

天津            117.12          39.08            tianjin

石家庄        114.29          38.02            shijiazhuang

唐山            118.01          39.38            tangshan

保定            115.29          38.51            baoding

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/a0cdafccdc8948018588722514cbda70.png)

所以在我们的网约车项目中，如果要实现叫车的功能，那么就可以使用Redis的GEO的功能，把经纬度的精度弄精确一段，就可以实现1000M之类的叫车之类的服务。

#### 底层原理

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/abc17ace31214f7789f7a95cedabc324.png)

GEO 类型的底层数据结构就是用 Sorted Set 来实现的。但是你发现一个问题，就是ZSET只有一个score的分数，那么GEO是如何表示精度和纬度两个值的呢！！！

核心就是Geohash编码

##### Geohash编码

比如北京 的经度和维度，（116.28，39.55），转化成二进制是0010 1101 0110 1100、1111 0111 0011，然后进行二进制的组合：第 0 位是经度的第 0 位 1，第 1 位是纬度的第 0 位 1，第 2 位是经度的第 1 位 1，第 3位是纬度的第 1 位 0，以此类推。最后得到最终的编码（大概的思想和方式）：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1699944921017/53ecb3ad7db44b12804f6e337817bf08.png)

然后就使用最后得出的这个二进制数组，保存为 Sorted Set 的权重分数。

```

```

##### 代码演示

com.msb.caffeine.lock.redistypes.GeoLocationTest

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1749187286039/bdf16c7dcdba42eaa79a487970ddabed.png)
