# Redis入门与应用

## Redis的版本选择与安装

在Redis的版本计划中，版本号第二位为奇数，为非稳定版本，如2.7、2.9、3.1；版本号第二为偶数，为稳定版本如2.6、2.8、3.0；一般来说当前奇数版本是下一个稳定版本的开发版本，如2.9是3.0的开发版本。

同时Redis的安装也非常简单，到Redis的官网（https://download.redis.io/releases/），下载对应的版本，简单几个命令安装即可。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/f7ff9554827540b8807bb273a30313a8.png)

课程里面还提供了Redis5版本的windows版本供大家玩（这个也是微软的开发者对应开发的，Redis官网也认，基本的功能也都有）

### Redis版本选择

总结就是，6的版本就够用了，高版本的话尽量选择偶数的小版本号的版本即可

#### **Redis 6 (2020年)**

重大更新，标志性版本。

核心特性包括：

多线程 I/O (处理网络请求) 、 ACL 访问控制列表 (细粒度权限) 、 TLS 加密支持 (传输安全) 、客户端缓存增强、RESP3 协议支持（新客户端协议）。这是目前很多生产环境仍在使用的稳定主力版本。

#### **Redis 7 (2022年)**

性能与功能增强。

核心特性包括： 多线程 I/O 成为默认选项 (性能提升) 、 Function (取代 EVAL/Lua 脚本的模块化编程) 、 Multi-Part AOF (AOF 文件分段，提高可靠性/加载速度) 、 Sharded Pub/Sub (集群模式下的广播消息) 、命令和配置项增强。推荐新项目使用的主流稳定版本。

#### Redis 8 (2024年)

最新稳定版本（截至2025年6月）。核心特性包括：

原生支持基于磁盘的键值存储 (作为内存的扩展) 、 新的集群管理命令和协议 (增强集群健壮性和管理) 、 Function 改进 (更多 API 和功能) 、 性能优化 (尤其在集群和大 key 场景) 、 RDB 文件格式优化 (v11) 、 更丰富的 ACL 能力 。

### **Redis的linux安装（这里是6.2.7，按照新版本也是一样）**

```
wget https://download.redis.io/releases/redis-6.2.7.tar.gz
tar xzf redis-6.2.7.tar.gz
cd redis-6.2.7/
make
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7cbc709d5bc4498fa11dc2d8aaa04ddb.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d56090bdd1fd4e90a1449c6c836d0d05.png)

安装后源码和执行目录会混在一起，为了方便，我做了一次install

```
make install PREFIX=/home/lijin/redis/redis
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/e697ede889734f18a1a9623e6b15f0dd.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7bb695c563e74321b6055e4eac3bee03.png)

因为Redis的安装一般来说对于系统依赖很少，只依赖了Linux系统基本的类库，所以安装很少出问题

**安装常见问题**

如果执行make命令报错：cc 未找到命令，原因是虚拟机系统中缺少gcc，执行下面命令安装gcc：

**Redis 6+ (尤其 7/8)：** 对 GCC 版本要求更高。**强烈建议使用 GCC 7.5 或更高版本（CentOS 7/RHEL 7 默认 GCC 4.8.5 可能无法编译或导致运行时问题）**

```
yum -y install gcc automake autoconf libtool make
```

如果执行make命令报错：致命错误:jemalloc/jemalloc.h: 没有那个文件或目录，则需要在make指定分配器为libc。执行下面命令即可正常编译：

```
make MALLOC=libc
```

make MALLOC=libc 是有效解决方案，但 jemalloc 通常是 Redis 推荐的内存分配器，因为它能更好地处理内存碎片，尤其在高负载下性能更优。

推荐解决 jemalloc 问题：安装 jemalloc 开发包：

```
sudo yum install jemalloc-devel   # RHEL/CentOS
sudo apt-get install libjemalloc-dev  # Debian/Ubuntu
```

清理之前的编译尝试：`make distclean`

重新编译：`make`

### Redis的启动

Redis编译完成后，会生成几个可执行文件，这些文件各有各的作用，我们现在先简单了解下，后面的课程会陆续说到和使用这些可执行文件。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/55847140e0b744c382acf8186fe4ffb9.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/210bdc3df1d941cea0d7f6449105310b.png)

一般来说redis-server和redis-cli这些平时用得最多。

Redis有三种方法启动Redis:默认配置、带参数启动、配置文件启动。

#### 默认配置

使用Redis的默认配置来启动，在bin目录下直接输入 ./redis-server

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/471e7f9ca54f4ace8f020d505014d602.png)

可以看到直接使用redis-server启动Redis后，会打印出一些日志，通过日志可以看到一些信息：

当前的Redis版本的是64位的6.2.7，默认端口是6379。Redis建议要使用配置文件来启动。

**因为直接启动无法自定义配置，所以这种方式是不会在生产环境中使用的。**

#### 带参数启动

redis-server加上要修改配置名和值(可以是多对)，没有设置的配置将使用默认配置，例如：如果要用6380作为端口启动Redis，那么可以执行:

./redis-server --port 6380

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d1096d2f70a444b1a16693a68c9daf5b.png)

这种方式一般我们也用得比较少。

#### 配置文件启动

配置文件是我们启动的最多的模式，配置文件安装目录中有

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/cb5bcbe8f3144cfda3720664fd58b13f.png)

复制过来

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/93c4ba2542e84a028ee31cbc2367dd48.png)

改一下权限

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/95e8ca2a9c284b0aaa33d224cc027bad.png)

通过配置文件来启动

```
./redis-server ../conf/redis.conf
```

注意：这里对配置文件使用了相对路径，绝对路径也是可以的。

同时配置文件的方式可以方便我们改端口，改配置，增加密码等。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/da1a238ae1844d5dac7e386896577d48.png)

打开注释，设置为自己的密码，重启即可

### 操作

Redis服务启动完成后，就可以使用redis-cli连接和操作Redis服务。redis-cli可以使用两种方式连接Redis服务器。

1、单次操作

用redis-cli -hip {host} -p{port} {command}就可以直接得到命令的返回结果，例如:

那么下一次要操作redis，还需要再通过redis-cli。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/0648f2e321764c82a13bc4b1b29c94cb.png)

2、命令行操作

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d38322174ebb48b89bbb8af2d80b8463.png)

通过redis-cli -h (host}-p {port}的方式连接到Redis服务，之后所有的操作都是通过控制台进行，例如:

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/816e9938ff2d49d9904ff79e413084bf.png)

我们没有写-h参数，那么默认连接127.0.0.1;如果不写-p，那么默认6379端口，也就是说如果-h和-p都没写就是连接127.0.0.1:6379这个 Redis实例。

### 停止

Redis提供了shutdown命令来停止Redis服务，例如我们目前已经启动的Redis服务，可以执行:

```
./redis-cli -p 6379 shutdown
```

redis服务端将会显示：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a066e0616af6479cb29e109feae91482.png)

除了可以通过shutdown命令关闭Redis服务以外，还可以通过kill进程号的方式关闭掉Redis，但是强烈不建议使用kill -9强制杀死Redis服务，不但不会做持久化操作，还会造成缓冲区等资源不能被优雅关闭，极端情况会造成AOF和复制丢失数据的情况。如果是集群，还容易丢失数据。

同样还可以在命令行中执行shutdown指令

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/caea54b05bd7468b93e5615fc506dafe.png)

shutdown还有一个参数,代表是否在关闭Redis前，生成持久化文件，缺省是save，生成持久化文件，如果是nosave则不生成持久化文件

## Redis全局命令

对于键值数据库而言，基本的数据模型是 key-value 模型，Redis 支持的 value 类型包括了 String、哈希表、列表、集合等，而Memcached支持的 value 类型仅为 String 类型，所以Redis 能够在实际业务场景中得到广泛的应用，就是得益于支持多样化类型的 value。

Redis里面有16个库，但是Redis的分库功能没啥意义（默认就是0号库，尤其是集群操作的时候），我们一般都是默认使用0号库进行操作。

在了解Rediskey-value 模型之前，Redis的有一些全局命令，需要我们提前了解。

**keys命令**

```
keys *
keys L*
```

查看所有键(支持通配符)：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a38baeb051bd46dab430d3037bedc48b.png)

但是这个命令请慎用，因为keys命令要把所有的key-value对全部拉出去，如果生产环境的键值对特别多的话，会对Redis的性能有很大的影响，推荐使用dbsize。

keys命令会遍历所有键，所以它的时间复杂度是o(n)，当Redis保存了大量键时线上环境禁止使用keys命令。

**dbsize命令**

dbsize命令会返回当前数据库中键的总数。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/21ed59cad4e84610869308640d57718c.png)

dbsize命令在计算键总数时不会遍历所有键,而是直接获取 Redis内置的键总数变量,所以dbsize命令的时间复杂度是O(1)。

**exists**

检查键是否存在，存在返回1，不存在返回0。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/706705c0c9d7418b8b72eb0be7228f85.png)

**del**

删除键，无论值是什么数据结构类型,del命令都可以将其删除。返回删除键个数，删除不存在键返回0。同时del命令可以支持删除多个键。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/2865421882494708bb0f78fd458a3a59.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/5d304a87b9d44633893581107f0d310a.png)

**键过期**

**expire**

Redis支持对键添加过期时间,当超过过期时间后,会自动删除键，时间单位秒。

ttl命令会返回键的剩余过期时间,它有3种返回值:

大于等于0的整数:键剩余的过期时间。

-1:键没设置过期时间。

-2:键不存在

除了expire、ttl命令以外，Redis还提供了expireat、pexpire,pexpireat、pttl、persist等一系列命令。

**expireat key**
timestamp: 键在秒级时间截timestamp后过期。

ttl命令和pttl都可以查询键的剩余过期时间，但是pttl精度更高可以达到毫秒级别，有3种返回值:

大于等于0的整数:键剩余的过期时间(ttl是秒，pttl是毫秒)。

-1:键没有设置过期时间。

-2:键不存在。

**pexpire key**
milliseconds:键在milliseconds毫秒后过期。

**pexpireat key**
milliseconds-timestamp键在毫秒级时间戳timestamp后过期。

**在使用Redis相关过期命令时,需要注意以下几点。**

1)如果expire key 的键不存在,返回结果为0:

2）如果过期时间为负值,键会立即被删除，犹如使用del命令一样:

3 ) persist命令可以将键的过期时间清除:

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/e288cca51dd446a19384a29adcc1faf7.png)

4）对于字符串类型键，执行set命令会去掉过期时间，这个问题很容易在开发中被忽视。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/5b749a4fd43248ce9af261080058ebaf.png)

5 ) Redis不支持二级数据结构(例如哈希、列表)内部元素的过期功能，不能对二级数据结构做过期时间设置。

**type**

返回键的数据结构类型，例如键lijin是字符串类型，返回结果为string。键mylist是列表类型，返回结果为list，键不存在返回none

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a5cf8aefd7b2414b9e1fb6ad96f24b47.png)

**randomkey**

随机返回一个键，这个很简单，请自行实验。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/41c50a66594e42f98755f4ca70814050.png)

**rename**

键重命名

但是要注意，如果在rename之前,新键已经存在，那么它的值也将被覆盖。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/ec04e5381693497b9d85040317c25d74.png)

为了防止被强行rename，Redis提供了renamenx命令，确保只有newKey不存在时候才被覆盖。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/41644a5ba52048e9a4b2d8b130d211a0.png)

从上面我们可以看出，由于重命名键期间会执行del命令删除旧的键，如果键对应的值比较大，会存在阻塞Redis的可能性。

### 键名的生产实践

Redis没有命令空间，而且也没有对键名有强制要求。但设计合理的键名，有利于防止键冲突和项目的可维护性，比较推荐的方式是使用“业务名:对象名: id : [属性]”作为键名(也可以不是分号)。、

例如MySQL 的数据库名为mall，用户表名为order，那么对应的键可以用"mall:order:1",
"mall:order:1:name"来表示，如果当前Redis 只被一个业务使用，甚至可以去掉“order:”。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/46d96dd72a2943ceba09f109f563c243.png)

在能描述键含义的前提下适当减少键的长度，从而减少由于键过长的内存浪费。

### 本节总结

见视频总结

## Redis常用数据结构

Redis提供了一些数据结构供我们往Redis中存取数据，最常用的的有5种，字符串（String）、哈希(Hash)、列表（list）、集合（set）、有序集合（ZSET）。

### 字符串（String）

字符串类型是Redis最基础的数据结构。首先键都是字符串类型，而且其他几种数据结构都是在字符串类型基础上构建的，所以字符串类型能为其他四种数据结构的学习奠定基础。字符串类型的值实际可以是字符串(简单的字符串、复杂的字符串(例如JSON、XML))、数字(整数、浮点数)，甚至是二进制(图片、音频、视频)，但是值最大不能超过512MB。

（虽然Redis是C写的，C里面有字符串&#x3c;本质使用char数组来实现>，但是处于种种考虑，Redis还是自己实现了字符串类型）

#### 操作命令

##### set 设置值

set key value![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/1dab86326fa249cd9d1ab118b48e9c6a.png)

set命令有几个选项:

ex seconds: 为键设置秒级过期时间。

px milliseconds: 为键设置毫秒级过期时间。

nx: 键必须不存在,才可以设置成功，用于添加（分布式锁常用）。

xx: 与nx相反,键必须存在，才可以设置成功,用于更新。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a37e8717b892401c8aff82af2d280ac9.png)

从执行效果上看，ex参数和expire命令基本一样。还有一个需要特别注意的地方是如果一个字符串已经设置了过期时间，然后你调用了set 方法修改了它，它的过期时间会消失。

而nx和xx执行效果如下

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/03c37295005c4bd19c33f7f7509778bc.png)

除了set选项，Redis 还提供了setex和 setnx两个命令:

setex key
seconds value

setnx key value

setex和 setnx的作用和ex和nx选项是一样的。也就是，setex为键设置秒级过期时间，setnx设置时键必须不存在,才可以设置成功。

setex示例：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/af158144826048b1ac8d2b43551d39a9.png)

setnx示例：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/864f3f2d1a5a43b5a16d3e71cb808352.png)

因为键foo-ex已存在,所以setnx失败,返回结果为0，键foo-ex2不存在，所以setnx成功,返回结果为1。

有什么应用场景吗?以setnx命令为例子，由于Redis的单线程命令处理机制，如果有多个客户端同时执行setnx key value，根据setnx的特性只有一个客户端能设置成功，setnx可以作为分布式锁的一种实现方案。当然分布式锁没有不是只有一个命令就OK了，其中还有很多的东西要注意，我们后面会用单独的章节来讲述基于Redis的分布式锁。

##### get 获取值

如果要获取的键不存在,则返回nil(空):

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/87d74c2cb95349f2a5191929c8eb7735.png)

##### mset 批量设置值

通过mset命令一次性设置4个键值对

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/21b4a825ec3e43898363ea98c322a512.png)

##### mget 批量获取值

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/36e86de36de446f19392c2e0bf8882a7.png)

批量获取了键a、b、c、d的值:

如果有些键不存在,那么它的值为nil(空)，结果是按照传入键的顺序返回。

批量操作命令可以有效提高效率，假如没有mget这样的命令，要执行n次get命令具体耗时如下:

n次 get时间=n次网络时间+n次命令时间

使用mget命令后，要执行n次get命令操作具体耗时如下:

n次get时间=1次网络时间+n次命令时间

Redis可以支撑每秒数万的读写操作，但是这指的是Redis服务端的处理能力，对于客户端来说，一次命令除了命令时间还是有网络时间，假设网络时间为1毫秒，命令时间为0.1毫秒(按照每秒处理1万条命令算)，那么执行1000次 get命令需要1.1秒(1000*1+1000*0.1=1100ms)，1次mget命令的需要0.101秒(1*1+1000*0.1=101ms)。

##### Incr 数字运算

incr命令用于对值做自增操作,返回结果分为三种情况：

值不是整数,返回错误。

值是整数，返回自增后的结果。

键不存在，按照值为0自增,返回结果为1。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/1073c166f7364cf99ebeacd03ba3ca16.png)

除了incr命令，Redis提供了decr(自减)、 incrby(自增指定数字)、decrby(自减指定数字)、incrbyfloat（自增浮点数)，具体效果请同学们自行尝试。

##### append追加指令

append可以向字符串尾部追加值

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/1c0468546f1e499d9f850e3eabffc351.png)

##### strlen 字符串长度

返回字符串长度

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7772d93625eb4680befbfb328129bbb4.png)

注意：每个中文占3个字节

##### getset 设置并返回原值

getset和set一样会设置值,但是不同的是，它同时会返回键原来的值

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d568dc6e0a6c4adeae13d8f63a8fd0d8.png)

##### setrange 设置指定位置的字符

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/bdd7efebb340433e807d61f970649218.png)

下标从0开始计算。

##### getrange 截取字符串

getrange 截取字符串中的一部分，形成一个子串，需要指明开始和结束的偏移量，截取的范围是个闭区间。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7802d85222fc450fbc4497d94acbd1fe.png)

#### 命令的时间复杂度

字符串这些命令中，除了del 、mset、 mget支持多个键的批量操作，时间复杂度和键的个数相关，为O(n)，getrange和字符串长度相关，也是O(n)，其余的命令基本上都是O(1)的时间复杂度，在速度上还是非常快的。

#### 使用场景

字符串类型的使用场景很广泛：

**缓存功能**

Redis 作为缓存层，MySQL作为存储层，绝大部分请求的数据都是从Redis中获取。由于Redis具有支撑高并发的特性,所以缓存通常能起到加速读写和降低后端压力的作用。

**计数**

使用Redis 作为计数的基础工具，它可以实现快速计数、查询缓存的功能,同时数据可以异步落地到其他数据源。

**共享Session**

一个分布式Web服务将用户的Session信息（例如用户登录信息)保存在各自服务器中，这样会造成一个问题，出于负载均衡的考虑，分布式服务会将用户的访问均衡到不同服务器上，用户刷新一次访问可能会发现需要重新登录，这个问题是用户无法容忍的。

为了解决这个问题,可以使用Redis将用户的Session进行集中管理,，在这种模式下只要保证Redis是高可用和扩展性的,每次用户更新或者查询登录信息都直接从Redis中集中获取。

**限速**

比如，很多应用出于安全的考虑,会在每次进行登录时,让用户输入手机验证码,从而确定是否是用户本人。但是为了短信接口不被频繁访问,会限制用户每分钟获取验证码的频率，例如一分钟不能超过5次。一些网站限制一个IP地址不能在一秒钟之内方问超过n次也可以采用类似的思路。

### 哈希(Hash)

Java里提供了HashMap，Redis中也有类似的数据结构，就是哈希类型。但是要注意，哈希类型中的映射关系叫作field-value，注意这里的value是指field对应的值，不是键对应的值。

#### 操作命令

基本上，哈希的操作命令和字符串的操作命令很类似，很多命令在字符串类型的命令前面加上了h字母，代表是操作哈希类型，同时还要指明要操作的field的值。

##### hset设值

hset user:1 name lijin

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/27d0a5b94e5e4a97a22b18f6ffbd370b.png)

如果设置成功会返回1，反之会返回0。此外Redis提供了hsetnx命令，它们的关系就像set和setnx命令一样,只不过作用域由键变为field。

##### hget取值

hget user:1 name

如果键或field不存在，会返回nil。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/fb8b44d85a8048e1a650c3e4c691036a.png)

##### hdel删除field

hdel会删除一个或多个field，返回结果为成功删除field的个数。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/45ed460d37c04b31bbd5122cd964a0e3.png)

##### hlen计算field个数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/348060ead6c44a7a82817187e00c2f18.png)

##### hmset批量设值

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/dbad743d5eed4f7eb98c39e345ba2687.png)

##### hmget批量取值

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/04c34b6e61004b32835ff377a05a5586.png)

##### hexists判断field是否存在

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7a182a0b69dc43c7a76f2119eca3910e.png)

若存在返回1，不存在返回0

##### hkeys获取所有field

它返回指定哈希键所有的field

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7f84f9381fcf48a0ac43af16dbea6b6c.png)

##### hvals获取所有value

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d4e3825c1bd446da9973b971834c37be.png)

##### hgetall获取所有field与value

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/338a05689b304021aa9b1cd6b601ffd9.png)

在使用hgetall时，如果哈希元素个数比较多，会存在阻塞Redis的可能。如果只需要获取部分field，可以使用hmget，如果一定要获取全部field-value，可以使用hscan命令，该命令会渐进式遍历哈希类型，hscan将在后面的章节介绍。

##### hincrby增加

hincrby和 hincrbyfloat，就像incrby和incrbyfloat命令一样，但是它们的作用域是filed。

##### hstrlen 计算value的字符串长度

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/4c52aa4d0f0c4841afa87011982c2151.png)

#### 命令的时间复杂度

哈希类型的操作命令中，hdel,hmget,hmset的时间复杂度和命令所带的field的个数相关O(k)，hkeys,hgetall,hvals和存储的field的总数相关，O(N)。其余的命令时间复杂度都是O(1)。

#### 使用场景

从前面的操作可以看出，String和Hash的操作非常类似，那为什么要弄一个hash出来存储。

哈希类型比较适宜存放对象类型的数据，我们可以比较下，如果数据库中表记录user为：

| id | name  | age |
| -- | ----- | --- |
| 1  | lijin | 18  |
| 2  | msb   | 20  |

**1、使用String类型**

需要一条条去插入获取。

set user:1:name lijin;

set user:1:age  18;

set user:2:name msb;

set user:2:age  20;

**优点：简单直观，每个键对应一个值**

**缺点：键数过多，占用内存多，用户信息过于分散，不用于生产环境**

**2、将对象序列化存入redis**

set user:1 serialize(userInfo);

**优点：编程简单，若使用序列化合理内存使用率高**

**缺点：序列化与反序列化有一定开销，更新属性时需要把userInfo全取出来进行反序列化，更新后再序列化到redis**

**3、使用hash类型**

hmset user:1 name lijin age 18

hmset user:2 name msb age 20

**优点：简单直观，使用合理可减少内存空间消耗**

**缺点：要控制内部编码格式，不恰当的格式会消耗更多内存**

### 列表（list）

列表( list)类型是用来存储多个有序的字符串，a、b、c、c、b四个元素从左到右组成了一个有序的列表,列表中的每个字符串称为元素(element)，一个列表最多可以存储(2^32-1)个元素(*4294967295*)。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/b2c5e0d8bd6243e59b0c32aa5caa49f1.png)

在Redis 中，可以对列表两端插入( push)和弹出(pop)，还可以获取指定范围的元素列表、获取指定索引下标的元素等。列表是一种比较灵活的数据结构，它可以充当栈和队列的角色，在实际开发上有很多应用场景。

**列表类型有两个特点:**

第一、列表中的元素是有序的，这就意味着可以通过索引下标获取某个元素或者某个范围内的元素列表。

第二、列表中的元素可以是重复的。

#### 操作命令

##### lrange 获取指定范围内的元素列表（不会删除元素）

key start end

索引下标特点：从左到右为0到N-1

lrange 0 -1命令可以从左到右获取列表的所有元素

##### rpush 向右插入

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/f495b4575e994d1f8623beba74dd6fdf.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/33e03ce96b3248ceaac60ccb4fc0b1d8.png)

##### lpush 向左插入

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/302233f9f0284e46ae26930ac8be1cd2.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/abba1d7c8aea4e42a83ca17476c4b9cd.png)

##### linsert 在某个元素前或后插入新元素

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/08d4476bb8e449b7b9efb82f271d69a4.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/c77d0ff5efe44cb8b19dd1d8bac12848.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/ebc063246d934ef19f6611f855273f45.png)

这三个返回结果为命令完成后当前列表的长度，也就是列表中包含的元素个数，同时rpush和lpush都支持同时插入多个元素。

##### lpop 从列表左侧弹出（会删除元素）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/cf3b7a2f1d85434689734dea9d467c0f.png)r

请注意，弹出来元素就没了。

##### rpop 从列表右侧弹出

rpop将会把列表最右侧的元素d弹出。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/6f55895f739b4a32a1e1c8fde0649031.png)

##### lrem 对指定元素进行删除

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/e533d9a779074c3286d81031ca408450.png)

lrem命令会从列表中找到等于value的元素进行删除，根据count的不同分为三种情况：

count>0，从左到右,删除最多count个元素。

count&#x3c;0，从右到左,删除最多count绝对值个元素。

count=0，删除所有。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/ad2833b4c011453f91928891e8a0e036.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/584b9986d80c46668f3fda91c466e340.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/ba19aa483a884537a687051f80967ebb.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/0480e571bc1e44f79b2e80b376ec288e.png)

返回值是实际删除元素的个数。

##### ltirm 按照索引范围修剪列表

例如想保留列表中第0个到第1个元素

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/8e52841e88f34a05888da5df0a8a4121.png)ls

##### lset修改指定索引下标的元素

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/663c682ce56444e382fc3f1b4da4253c.png)

##### lindex 获取列表指定索引下标的元素

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/f0b6ab8eca224305bcd97a4916c33947.png)l

##### llen 获取列表长度

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/31f34b4b3ec24f2e89d42b958dc3f343.png)

##### blpop和brpop阻塞式弹出元素

blpop和brpop是lpop和rpop的阻塞版本，除此之外还支持多个列表类型，也支持设定阻塞时间，单位秒，如果阻塞时间为0，表示一直阻塞下去。我们以brpop为例说明。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a83e89040af7495da20b56ef826e6520.png)

A客户端阻塞了（因为没有元素就会阻塞）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/c11fe0ec650c401cbd69500d99805f85.png)

A客户端一直处于阻塞状态。此时我们从另一个客户端B执行

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/c4c83e7fe2ac4f6385ac04b1ecbcf231.png)

A客户端则输出

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/4b2c7c7b95da4190ab6c772075faec7c.png)

注意：brpop后面如果是多个键，那么brpop会从左至右遍历键，一旦有一个键能弹出元素，客户端立即返回。

#### 使用场景

列表类型可以用于比如：

消息队列，Redis 的 lpush+brpop命令组合即可实现阻塞队列，生产者客户端使用lrpush从列表左侧插入元素，多个消费者客户端使用brpop命令阻塞式的“抢”列表尾部的元素,多个客户端保证了消费的负载均衡和高可用性。

**文章列表**

每个用户有属于自己的文章列表，现需要分页展示文章列表。此时可以考虑使用列表,因为列表不但是有序的,同时支持按照索引范围获取元素。

实现其他数据结构

lpush+lpop =Stack（栈)

lpush +rpop =Queue(队列)

lpsh+ ltrim =Capped Collection（有限集合)

lpush+brpop=Message Queue(消息队列)

### 集合（set）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/4b28f3a2aa6b4d4f9c5dd09317508102.png)

集合( set）类型也是用来保存多个的字符串元素,但和列表类型不一样的是，集合中不允许有重复元素,并且集合中的元素是无序的,不能通过索引下标获取元素。

一个集合最多可以存储2的32次方-1个元素。Redis除了支持集合内的增删改查，同时还支持多个集合取交集、并集、差集，合理地使用好集合类型,能在实际开发中解决很多实际问题。

#### 集合内操作命令

##### sadd 添加元素

允许添加多个，返回结果为添加成功的元素个数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d94f1e8871054f6dad9dad1d245d3dfb.png)

##### srem 删除元素

允许删除多个，返回结果为成功删除元素个数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/fdcc39ccdf4c4ca1bb26003e65d40ec7.png)

##### scard 计算元素个数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/292819f68b294180ad037dc590e8c562.png)

##### sismember 判断元素是否在集合中

如果给定元素element在集合内返回1，反之返回0

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/94148d18517a44d99cd6c7716d07f9b1.png)

##### srandmember 随机从集合返回指定个数元素

指定个数如果不写默认为1

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/2b386e0de6df44c685dc69e95bf4e7a2.png)

##### spop 从集合随机弹出元素

同样可以指定个数，如果不写默认为1，注意，既然是弹出，spop命令执行后,元素会从集合中删除,而srandmember不会。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/172580272c8b4413908328fad51e0537.png)

##### smembers 获取所有元素(不会弹出元素)

返回结果是无序的

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/f16f1615505b49a2aa5198a75168c4b2.png)

#### 集合间操作命令

现在有两个集合,它们分别是set1和set2

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/8546a5e8eff348bd8c4879fbc37db67b.png)

##### sinter 求多个集合的交集

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/80dd47f94b06433daa0b1f19be6a684f.png)

##### suinon 求多个集合的并集

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/614e67ffe97646389a781099b6e074fd.png)

##### sdiff 求多个集合的差集

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/4c0e783c37e64c15866bb08ccc0abbf8.png)

##### 将交集、并集、差集的结果保存

```
sinterstore destination key [key ...]
suionstore destination key [key ...]
sdiffstore destination key [key ...]

```

集合间的运算在元素较多的情况下会比较耗时，所以 Redis提供了上面三个命令(原命令+store)将集合间交集、并集、差集的结果保存在destination key中，例如：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/450fcb6c2e2342b39925c1779a4ba369.png)

#### 使用场景

集合类型比较典型的使用场景是标签(tag)。例如一个用户可能对娱乐、体育比较感兴趣，另一个用户可能对历史、新闻比较感兴趣，这些兴趣点就是标签。有了这些数据就可以得到喜欢同一个标签的人，以及用户的共同喜好的标签，这些数据对于用户体验以及增强用户黏度比较重要。

例如一个电子商务的网站会对不同标签的用户做不同类型的推荐，比如对数码产品比较感兴趣的人，在各个页面或者通过邮件的形式给他们推荐最新的数码产品，通常会为网站带来更多的利益。

除此之外，集合还可以通过生成随机数进行比如抽奖活动，以及社交图谱等等。

### 有序集合（ZSET）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/fe24d4258f4b4dd99ad52a6752a840ae.png)

有序集合相对于哈希、列表、集合来说会有一点点陌生,但既然叫有序集合,那么它和集合必然有着联系,它保留了集合不能有重复成员的特性,但不同的是,有序集合中的元素可以排序。但是它和列表使用索引下标作为排序依据不同的是,它给每个元素设置一个分数( score)作为排序的依据。

有序集合中的元素不能重复，但是score可以重复，就和一个班里的同学学号不能重复,但是考试成绩可以相同。

有序集合提供了获取指定分数和元素范围查询、计算成员排名等功能，合理的利用有序集合，能帮助我们在实际开发中解决很多问题。

#### 集合内操作命令

##### zadd添加成员

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/353cb7b71ca04daab3cf7fb99ceb36f6.png)

返回结果代表成功添加成员的个数

要注意:

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a8a349c237304f6c94f0a16f5ae3ed24.png)

zadd命令还有四个选项nx、xx、ch、incr 四个选项

nx: member必须不存在，才可以设置成功，用于添加。

xx: member必须存在，才可以设置成功,用于更新。

ch: 返回此次操作后,有序集合元素和分数发生变化的个数

incr: 对score做增加，相当于后面介绍的zincrby

##### zcard 计算成员个数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/9eb0d86663fd45e6a3032c05c1870339.png)

##### zscore 计算某个成员的分数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7da7b741030b4b2b9863d569198f001d.png)

如果成员不存在则返回nil

##### zrank计算成员的排名

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/7395eb62a4fa4e669cf201799aca069e.png)

zrank是从分数从低到高返回排名

zrevrank反之

很明显，排名从0开始计算。

##### zrem 删除成员

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d4e23637dda8461abba76c303cfbfbad.png)

允许一次删除多个成员。

返回结果为成功删除的个数。

##### zincrby 增加成员的分数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/b1a0193040334a0f90ddb66bce53fe0c.png)

##### zrange和zrevrange返回指定排名范围的成员

有序集合是按照分值排名的，zrange是从低到高返回,zrevrange反之。如果加上
withscores选项，同时会返回成员的分数

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/d2a8d64fce484b64bf95f0a886fa1f45.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/a17fc90c48dd498f9539f5b834ada3bb.png)

##### zrangebyscore返回指定分数范围的成员

```
zrangebyscore key min max [withscores] [limit offset count]
zrevrangebyscore key max min [withscores][limit offset count]

```

其中zrangebyscore按照分数从低到高返回，zrevrangebyscore反之。例如下面操作从低到高返回200到221分的成员，withscores选项会同时返回每个成员的分数。

同时min和max还支持开区间(小括号）和闭区间(中括号)，-inf和+inf分别代表无限小和无限大:

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/de92b2a82134468ab69bbb8718ccfd1f.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/92edbaef3b2c4c4c9a7ee0daac41ad0a.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/e5dc53cdbff941c9814590973ac96499.png)

##### zcount 返回指定分数范围成员个数

zcount key min max

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/82a1a39c69bf4c8395411312fd276515.png)

##### zremrangebyrank 按升序删除指定排名内的元素

zremrangebyrank key start end

##### zremrangebyscore 删除指定分数范围的成员

zremrangebyscore key min max

#### 集合间操作命令

##### zinterstore 交集

zinterstore![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/0e0688f60b404126895ffbbf4c6ae290.png)

这个命令参数较多，下面分别进行说明

destination:交集计算结果保存到这个键。

numkeys:需要做交集计算键的个数。

key [key ...]:需要做交集计算的键。

weights weight
[weight ...]:每个键的权重，在做交集计算时，每个键中的每个member 会将自己分数乘以这个权重,每个键的权重默认是1。

aggregate sum/
min |max:计算成员交集后，分值可以按照sum(和)、min(最小值)、max(最大值)做汇总,默认值是sum。

不太好理解，我们用一个例子来说明。（算平均分）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/cfc3967b70cb4dcea41057c3708d8616.png)

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1663252342001/65f2bee807e1446989277c0fadff86cf.png)

##### zunionstore 并集

该命令的所有参数和zinterstore是一致的，只不过是做并集计算，大家可以自行实验。

#### 使用场景

有序集合比较典型的使用场景就是排行榜系统。例如视频网站需要对用户上传的视频做排行榜，榜单的维度可能是多个方面的:按照时间、按照播放数量、按照获得的赞数。

## Redis的Java常用客户端

Redis 官方推荐的Java 客户端 Jedis、lettuce 和 Redisson

### Jedis

老牌的Redis 的Java客户端，提供了比较全面的Redis命令的支持

**优点：**

API比较全面（参考：[https://tool.oschina.net/uploads/apidocs/redis/clients/jedis/Jedis.html](https://tool.oschina.net/uploads/apidocs/redis/clients/jedis/Jedis.html)）

**缺点：**

使用阻塞的 I/O（方法调用都是同步的，程序流需要等到 sockets 处理完 I/O 才能执行，不支持异步）

Jedis 客户端实例不是线程安全的（多线程使用一个Jedis连接），所以需要通过连接池来使用Jedis（每个线程使用独自的Jedis连接）

### lettuce

lettuce是基于netty实现的与redis进行同步和异步的通信。

在spring boot2之后，redis连接默认就采用了lettuce（spring-boot-starter-data-redis）

官网：[https://lettuce.io/](https://lettuce.io/)

github：[https://github.com/lettuce-io/lettuce-core](https://github.com/lettuce-io/lettuce-core)

SpringData：[https://spring.io/projects/spring-data-redis](https://spring.io/projects/spring-data-redis)

**优点：**

线程安全的 Redis 客户端，支持异步模式

lettuce 底层基于 Netty，支持高级的 Redis 特性，比如哨兵，集群，管道，自动重新连接和Redis数据模型。

**缺点：**

没人知道... API比较复杂

### Redisson

Redisson 提供了使用Redis 的最简单和最便捷的方法，还提供了许多分布式服务（分布式锁，分布式集合，延迟队列等）

**优点：**

Redisson基于Netty框架的事件驱动的通信层，其方法调用是异步的

Redisson的API是线程安全的，所以可以操作单个Redisson连接来完成各种操作

**缺点：**

Redisson 对字符串的操作支持比较差

推荐文档：[https://redisson.org/docs/](https://redisson.org/docs/)

[redisson中文API](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E9%94%81%E5%92%8C%E5%90%8C%E6%AD%A5%E5%99%A8#84-%E7%BA%A2%E9%94%81redlock)

## 项目整合spring-boot-starter-data-redis

&emsp;&emsp;要整合Redis那么我们在SpringBoot项目中首页来添加对应的依赖

```xml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```

&emsp;&emsp;然后我们需要添加对应的配置信息

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/1462/1647243560000/753f65df9029411a8145b2477d1e58d3.png)

测试操作Redis的数据

```java
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate(){
        // 获取操作String类型的Options对象
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 插入数据
        ops.set("name","lijin"+ UUID.randomUUID());
        // 获取存储的信息
        System.out.println("刚刚保存的值："+ops.get("name"));
    }
```

查看可以通过Redis的客户端连接查看

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/1462/1647243560000/1b678ea26d684404a93217b8bb781164.png)

也可以通过工具查看（大家自行搜索去找，有收费的，有免费的）

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/1462/1647243560000/33e044f827f0470bb9f3a27308f77118.png)

## 项目整合Redisson

添加对应的依赖

```xml
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.16.1</version>
        </dependency>
```

添加对应的配置类

```java
@Configuration
public class MyRedisConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        // 配置连接的信息
        config.useSingleServer()
                .setAddress("redis://192.168.56.100:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return  redissonClient;
    }
}
```

案例代码

POM文件 片段

```
     <!-- spring-redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- redisson -->
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.30.0</version>
        </dependency>
        <!-- jedis -->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>3.6.3</version>
        </dependency>
```

test代码

```
package com.msb.caffeine.lock;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class TestClient {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Test
    public void JedisDemo() {

        Jedis jedis =  new Jedis("127.0.0.1",6379);

        // 设置字符串值
        jedis.del("key1");
        jedis.set("key1", "value1");
        // 获取字符串值
        String value = jedis.get("key1");
        System.out.println(value);

        // 向列表中插入元素
        jedis.del("list1");
        jedis.lpush("list1", "element1", "element2", "element3");
        // 获取列表中的所有元素
        List elements = jedis.lrange("list1", 0, -1);
        System.out.println(elements);

        // 向集合中添加元素
        jedis.del("set1");
        jedis.sadd("set1", "member1", "member2", "member3");
        // 获取集合中的所有元素
        Set members = jedis.smembers("set1");
        System.out.println(members);


        // 设置哈希字段值
        jedis.del("hash1");
        jedis.hset("hash1", "field1", "value1");
        // 获取哈希字段值
        String hashValue = jedis.hget("hash1", "field1");
        System.out.println(hashValue);


        // 向有序集合中添加元素
        jedis.del("zset1");
        jedis.zadd("zset1", 100, "player1");
        jedis.zadd("zset1", 200, "player2");
        jedis.zadd("zset1", 150, "player3");

        // 获取有序集合中的排名榜
        Set<Tuple> ranking = jedis.zrevrangeWithScores("zset1", 0, -1);

        // 展示排名榜
        int rank = 1;
        for (Tuple tuple : ranking) {
            String player = tuple.getElement();
            double score = tuple.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }
    @Test
    public void RedisTemplateDemo() {


        // 设置字符串值
        redisTemplate.delete("key1");
        redisTemplate.opsForValue().set("key1", "value1");

        stringRedisTemplate.opsForValue().increment("lijin",1);

        // 获取字符串值
        String value = redisTemplate.opsForValue().get("key1").toString();
        System.out.println(value);

        // 向列表中插入元素
        redisTemplate.delete("list1");
        redisTemplate.opsForList().leftPushAll("list1", "element1", "element2", "element3");
        // 获取列表中的所有元素
        List<String> elements = redisTemplate.opsForList().range("list1", 0, -1);
        System.out.println(elements);

        // 向集合中添加元素
        redisTemplate.delete("set1");
        redisTemplate.opsForSet().add("set1", "member1", "member2", "member3");
        // 获取集合中的所有元素
        Set<String> members = redisTemplate.opsForSet().members("set1");
        System.out.println(members);

        // 设置哈希字段值
        redisTemplate.delete("hash1");
        redisTemplate.opsForHash().put("hash1", "field1", "value1");
        // 获取哈希字段值
        String hashValue = (String) redisTemplate.opsForHash().get("hash1", "field1");
        System.out.println(hashValue);

        // 向有序集合中添加元素
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();


        redisTemplate.delete("zset1");
        zSetOperations.add("zset1", "player1", 100);
        zSetOperations.add("zset1", "player2", 200);
        zSetOperations.add("zset1", "player3", 150);
        // 获取有序集合中的排名榜
        Set<ZSetOperations.TypedTuple<Object>> ranking = zSetOperations.reverseRangeWithScores("zset1", 0, -1);
        // 展示排名榜
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : ranking) {
            String player = tuple.getValue().toString();
            double score = tuple.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }

    @Test
    public void RedissonDemo() {
        // 设置字符串值
        redisson.getBucket("key1").delete();
        redisson.getBucket("key1").set("value1");
        // 获取字符串值
        String value = redisson.getBucket("key1").toString();
        System.out.println(value);

        // 向列表中插入元素
        RList<String> list = redisson.getList("list1");
        list.delete();
        list.add("element1");
        list.add("element2");
        list.add("element3");
        // 获取列表中的所有元素
        List<String> elements = list.readAll();
        System.out.println(elements);

        // 向集合中添加元素
        RSet<String> set = redisson.getSet("set1");
        set.delete();
        set.add("member1");
        set.add("member2");
        set.add("member3");
        // 获取集合中的所有元素
        Set<String> members = set.readAll();
        System.out.println(members);

        // 设置哈希字段值
        RMap<String, String> map = redisson.getMap("hash1");
        map.delete();
        map.put("field1", "value1");
        // 获取哈希字段值
        String hashValue = map.get("field1");
        System.out.println(hashValue);

        // 向有序集合中添加元素
        RScoredSortedSet<String> zset = redisson.getScoredSortedSet("zset1");
        zset.delete();
        zset.add(100, "player1");
        zset.add(200, "player2");
        zset.add(150, "player3");
        // 获取有序集合中的排名榜
        Collection<ScoredEntry<String>> ranking = zset.entryRangeReversed(0, -1);
        // 展示排名榜
        int rank = 1;
        for (ScoredEntry<String> entry : ranking) {
            String player = entry.getValue();
            double score = entry.getScore();
            System.out.println("排名：" + rank + "，玩家：" + player + "，得分：" + score);
            rank++;
        }
    }


    @Test
    public void LockExample() {
        long goods_id =13;
        String Lockkey ="goods-number"+goods_id;
        RLock lock = redisson.getLock(Lockkey);  //Lockkey 这个是标识 锁的名称
        lock.lock();//这种代码下 是会开启看门狗的（默认的10秒运行一次 进行续锁， 这种Redis的TTL30秒）
        //lock.lock(30, TimeUnit.SECONDS); //这种就不会启动看门狗了

        log.info("业务代码1");

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
       }
        log.info("业务代码2");

        lock.unlock();
        log.info("业务代码3");

    }

    @Test
    public void AtomicExample() throws Exception{
        RAtomicLong atomicLong = redisson.getAtomicLong("myAtomicLong");
        atomicLong.set(3);
        atomicLong.incrementAndGet();
        log.info(String.valueOf(atomicLong.get()));

//        RAtomicDouble atomicDouble = redisson.getAtomicDouble("myAtomicDouble");
//        atomicDouble.set(2.81);
//        atomicDouble.addAndGet(4.11);
//        atomicDouble.get();
//
//        RLongAdder atomicLong2 = redisson.getLongAdder("myLongAdder");
//        for (int i=0;i<10;i++){
//            Thread thread = new Thread(()->{
//                atomicLong2.increment();
//            });
//            thread.start();
//        }
//
//        Thread.sleep(1000);
//        log.info(String.valueOf(atomicLong2.sum()));
    }

    @Test
    public void RateLimiterExample() throws Exception {
        RRateLimiter rateLimiter = redisson.getRateLimiter("myRateLimiter");
        // 初始化
        // 最大流速 = 每5秒钟产生10个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 10, 5, RateIntervalUnit.SECONDS);

        try {
            Thread.sleep(3000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while(true){
            rateLimiter.acquire(1);
            Thread.sleep(500);
            log.info("获取令牌成功");
        }

    }

    @Test
    public void CountDownLatchExample() throws Exception{
        RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");
        latch.trySetCount(1);
        latch.await();
        log.info("are you OK?");
    }

    @Test
    public void SemaphoreExample() throws Exception{
        RSemaphore lock = redisson.getSemaphore("Semaphore1");
        //lock.trySetPermits(10);//这里就是先放10个信号
        for (int i = 0; i < 10; i++) {
            double random = Math.random();
            System.out.println(random);

            //如果随机数大于0.5则获取，否则释放
            if (random > 0.5) {
                boolean b = lock.tryAcquire();//尝试获取，如果没有，就不获取
                if (b) {
                    log.info("acquire...");
                } else {
                    log.info("未获取到...");
                }

            } else {
                lock.release();
                log.info("release...");
            }
        }
    }


}

```

```
package com.msb.caffeine.config;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {
    /**
     * 所有对Redisson的使用都是通过RedissonClient
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson(){
        //1、创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        //2、根据Config创建出RedissonClient实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}

```

```
package com.msb.caffeine.config;

import com.msb.caffeine.service.RedisMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(); // 需要设置主机名，端口，密码等参数
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化对象
        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        // 设置键序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置值序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory, RedisMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listener, new ChannelTopic("cacheUpdateChannel"));
        return container;
    }
}

```

## 学习总结
