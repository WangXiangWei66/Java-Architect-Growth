# MySQL数据库架构师-基础知识

本课程以实际项目作为教学案例《飞滴出行网约车项目》

课程地址：https://www.mashibing.com/course/1537

课程资料：可以下载到对应的SQL。

## E-R模型设计

### E-R模型核心概念

**基本构成元素分析**

1. **实体(Entity)** ：

   * 可区分业务对象 → 对应MySQL表
   * 飞滴出行网约车项案例：`passenger_user`（乘客）、`driver_user`（司机）、`car`（车辆）
   * 标识：主键 `id BIGINT UNSIGNED AUTO_INCREMENT`
2. **属性(Attribute)** ：

   * 实体特征 → 对应表字段
   * 原子性约束：如 `address`拆分为省市区编码（关联 `dic_district`）
   * 飞滴出行网约车项案例：`driver_phone`（手机号）、`vehicle_no`（车牌号）...
3. **关系(Relationship)** ：

   * 一对一(1:1)：司机与车辆绑定（如果业务规则做强制限制一个司机只能有一辆车，一辆车只能有一个司机的话）
   * 一对多(1:N)：乘客→订单（`order_info.passenger_id`）
   * 多对多(M:N)：司机-车辆关系（`driver_car_binding_relationship`表）

### E-R模型中高级概念

**弱实体：弱实体（Weak Entity）指**无独立主键**的实体集，其存在完全依赖另一个 **强实体集** （Owner Entity），两者通过 **标识性联系** （Identifying Relationship）绑定**

* 司机工作状态（`driver_user_work_status`）依赖司机主体
* 司机车辆绑定关系(driver_car_binding_relationship)依赖于司机和车辆

### E-R设计流程

1. **局部E-R设计** ：
   * 模块划分：用户模块（乘客/司机）、车辆模块、订单模块
   * 实体定义：乘客属性（手机号、姓名、性别）
2. **全局E-R集成** ：
   * 冲突解决：
     * 属性冲突：统一 `gmt_create`时间格式（datetime(0)）
     * 命名冲突：所有表主键命名 `id`
     * 结构冲突：车辆状态字段统一用 `state TINYINT`

### 实体属性取舍

* **属性升格案例** ：

  * 民族代码 → 独立 `dic_nation`表（原可作为 `driver_user`属性）
  * 地区编码 → `dic_district`表（满足多级关联需求）
*
* **关系属性分配** ：

  * M:N关系属性：司机-车辆绑定时间 → `driver_car_binding_relationship.binding_time`
  * 1:N关系属性：订单价格 → `order_info.price`

## 范式与反范设计

### 什么是范式

范式来自英文Normal Form，简称NF。

实际上你可以把它粗略地理解为 **一张数据表的表结构所符合的某种设计标准的级别** 。就像家里装修买建材，最环保的是E0级，其次是E1级，还有E2级等等

目前关系数据库有六种范式：第一范式（1NF）、第二范式（2NF）、第三范式（3NF）、巴斯-科德范式（BCNF）、第四范式(4NF）和第五范式5NF，又称完美范式）。

满足最低要求的范式是第一范式（1NF），在第一范式的基础上进一步满足更多规范要求的称为第二范式（2NF），其余范式以次类推。一般来说，数据库只需满足第三范式(3NF）就行了。![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/85d0baa6c50443d292669b21453b4553.png)

### 项目中范式分析

本课程以实际项目作为教学案例《飞滴出行网约车项目》

课程地址：https://www.mashibing.com/course/1537

课程资料：可以下载到对应的SQL。

#### 1. **第一范式（1NF）**

**定义： 属于第一范式关系的所有属性都不可再分，即数据项不可分。**

所有表均满足1NF要求

比如：

```
driver_user表
driver_phone VARCHAR(16)  -- 单一手机号属性
driver_gender TINYINT     -- 单一性别属性
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/f97cf53d62f44a209ed4897e8fdfb052.png)

`order_info`表的坐标字段（如 `dep_longitude`）虽为字符串，但表示单一坐标值，符合原子性。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/5a592e3ea42249b296e304f38d1926f2.png)

就是简单的说，只要不要有这种 `name-age`的复合列字段，就不会违反第一范式。

#### 2. **第二范式（2NF）**

满足第二范式（2NF）必须先满足第一范式（1NF）。

**定义：第二范式（2NF）要求实体的属性完全依赖于主关键字。**

所有表均满足2NF要求

| `price_rule` | `(city_code, vehicle_type)` | 起步价等完全依赖城市+车型组合 |
| -------------- | ----------------------------- | ----------------------------- |

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/88877550ea664e8ebc7c1aba8f8f801c.png)

| `order_info` | `id` | 所有订单字段完全依赖订单ID |
| -------------- | ------ | -------------------------- |

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/f6cb7a4712db4517acd602edbca4e5b0.png)

#### 3. 第三范式（3NF）

满足第三范式（3NF）必须先满足第二范式（2NF）

**定义：第三范式（3NF）要求一个数据库表中不包含已在其它表中包含的非主关键字信息，即数据不能存在传递关系，即每个属性都跟主键有直接关系而不是间接关系。**

多数表满足3NF，但存在 **反范式优化** ：

满足第三范式的表：

```
dic_district：地区信息无传递依赖
driver_user：司机属性直接依赖主键
```

不满足第三范式的表：

```
order_info：存在冗余字段（反范式优化）
```

完全符合范式化的设计真的完美无缺吗？很明显在实际的业务查询中会大量存在着表的关联查询，而表设计都做成了范式化设计（甚至很高的范式），大量的表关联很多的时候非常影响查询的性能。

**反范式化就是违反范式化设计：**

1、为了性能和读取效率而适当的违反对数据库设计范式的要求；

2、为了查询的性能，允许存在部分（少量）冗余数据

**换句话来说反范式化就是使用空间来换取时间。**

#### 4. 反范式设计分析

**反范式优化表：`order_info`**

1. 冗余数据：`passenger_phone`可关联 `passenger_user`表获取
2. 冗余数据：`driver_phone`可关联 `driver_user`表获取
3. 冗余数据：`address`可关联 `dic_district`表获取

**设计理由** （空间换时间）：

* **高频查询** ：订单详情页需实时展示司机/乘客手机号、地址相关信息
* **性能考量** ：避免多表关联（`order_info`+`driver_user`+`passenger_user`）
* **数据稳定性** ：手机号更新频率低，一致性风险可控
* **历史数据需求**：如果司机或者乘客更换手机号，订单表中显示的就是历史手机信息

**反范式适用场景**

* ✅  **高频查询字段** ：如订单中的联系方式
* ✅  **极少更新字段** ：如车牌号、用户姓名
* ✅  **统计类字段** ：如订单总金额（若需实时计算）

**反范式禁忌场景**

* ❌  **高频更新字段** ：如账户余额
* ❌  **大文本字段** ：如订单备注
* ❌  **强一致性要求** ：如金融交易流水

## 字段类型优化

本课程以实际项目作为教学案例《飞滴出行网约车项目》

课程地址：https://www.mashibing.com/course/1537

课程资料：可以下载到对应的SQL。

### 1. **更小的数据类型**

一般情况下,应该尽量使用可以正确存储数据的最小数据类型。更小的数据类型通常更快，因为它们占用更少的磁盘、内存和CPU缓存，并且处理时需要的CPU周期也更少。

* 所有状态字段(如 `state`、`fix_state`等)可以推荐使用 `TINYINT`而非 `INT`

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/a2caba344d9340aea1c444c618226ce5.png)
* 枚举值字段(如 `plate_color`)使用 `CHAR(1)`而非 `VARCHAR`

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/b16c4e60cc8b4ef696e199abe30de568.png)

### 2. **简单类型优先**

简单数据类型的操作通常需要更少的CPU周期。例如，整型比字符操作代价更低，因为字符集和校对规则(排序规则)使字符比较比整型比较更复杂。比如应该使用MySQL内建的类型而不是字符串来存储日期和时间。

存储整数，可以使用这几种整数类型:TINYINT，SMALLINT，MEDIUMINT，INT，BIGINT。分别使用8，16，24，32，64位存储空间，也就是1、2、3、4、8个字节。它们可以存储的值的范围请自行计算。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/8ab773b79c2a4c4d949c26e608f5d802.png)

* 日期时间统一使用 `DATETIME`类型

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/ce1f473bacb4475b9b3492f654ed86d8.png)
* 数值类型优先于字符串存储编码类数据

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/75e911cbe9f041b2a7fcb355a851de21.png)

### 3. **避免NULL值**

通常情况下最好指定列为NOT NULL，除非真的需要存储NULL值。

如果查询中包含可为NULL的列，对MySQL来说更难优化，因为可为NULL的列使得索引、索引统计和值比较都更复杂。可为NULL的列会使用更多的存储空间，在MySQL里也需要特殊处理。当可为NULL的列被索引时，每个索引记录需要一个额外的字节。

`car` 车辆表优化

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/0d5e2e1c66e14b2c9a3ecedf700d3338.png)

```
-- 原字段
`plate_color` char(1) NULL
`fix_state` char(2) NULL
`check_state` char(2) NULL DEFAULT ''
`commercial_type` int NULL

-- 优化后
`plate_color` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '车牌颜色（1：蓝，2：黄...）'
`fix_state` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '检修状态(0：未检，1：已检)'
`check_state` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '年审状态（0：未审，1：合格）'
`commercial_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '服务类型'
```

**优化理由** ：

1. 状态字段使用 `TINYINT`节省75%存储空间（1字节 vs 4字节）
2. `NOT NULL`避免索引处理NULL的额外开销
3. 默认值确保数据完整性

### 4、字符串类型优化

固定长度编码使用CHAR

```
-- 地区编码优化
`address_code` CHAR(6) NOT NULL COMMENT '地区编码' 
```

短字符串精确长度

```
-- 车辆号牌优化
`vehicle_no` VARCHAR(8) NOT NULL DEFAULT '' COMMENT '车牌号' -- 国内车牌最长8字符
```

避免过度分配

```
-- 驾驶员姓名优化
`driver_name` VARCHAR(16) NOT NULL DEFAULT '' COMMENT '司机姓名' -- 原VARCHAR(255)过大
```

### 5、金额字段优化方案

DOUBLE有精度损失问题，金融计算不可接受

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1751265760026/8cfe1df73ebf44338c3e20c943df1406.png)

```
`start_fare` DECIMAL(6,2) UNSIGNED NOT NULL DEFAULT 0.00   - 使用DECIMAL精确存储

```

## 命名规范

1、可读性原则

数据库、表、字段的命名要遵守可读性原则，尽可能少使用或者不使用缩写。

对象的名字应该能够描述它所表示的对象。例如：表的名称应该能够体现表中存储的数据内容，最好是遵循“业务名称_表的作用”；对于存储过程存储过程应该能够体现存储过程的功能。库名与应用名称尽量一致。

表达是与否概念的字段，应该使用is_xxx的方式命名，数据类型是unsigned tinyint（1表示是，0表示否）。

2、表名、字段名必须使用小写字母或数字，禁止出现数字开头，禁止两个下划线中间只出现数字。数据库字段名的修改代价很大，因为无法进行预发布，所以字段名称需要慎重考虑。
说明：MySQL在Windows下不区分大小写，但在Linux下默认是区分大小写。因此，数据库名、表名、字段名，都不允许出现任何大写字母，避免节外生枝。

3、表名不使用复数名词

4、数据库、表、字段的命名禁用保留字，如desc、range、match之类

6、主键索引名为pk_字段名；唯一索引名为uk_字段名；普通索引名则为idx_字段名。

## MySql中索引与B+树

MySQL官方对索引的定义为：索引（Index）是帮助MySQL高效获取数据的数据结构。可以得到索引的本质： **索引是数据结构** 。InnoDB存储引擎支持以下几种常见的索引：B+树索引、全文索引、哈希索引，其中比较关键的是B+树索引

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/ab345e828dd043b7a89748587f993e6c.png)

**那为什么HashMap不适合做数据库索引？**

1、hash表只能匹配是否相等，不能实现范围查找；

2、当需要按照索引进行order by时，hash值没办法支持排序；

3、组合索引可以支持部分索引查询，如(a,b,c)的组合索引，查询中只用到了a和b也可以查询的，如果使用hash表，组合索引会将几个字段合并hash，没办法支持部分索引；

4、当数据量很大时，hash冲突的概率也会非常大。

### 二分查找

二分查找法（binary search） 也称为折半查找法，用来查找一组有序的记录数组中的某一记录。

在以下数组中找到数字48对应的下标

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/22adb01ec5c34eff8c432e3c872ec2d8.png)

通过3次二分查找 就找到了我们所要的数字，而顺序查找需8次。

对于上面10个数来说，顺序查找平均查找次数为（1+2+3+4+5+6+7+8+9+10)/10=5.5次。而二分查找法为(4+3+2+4+3+1+4+3+2+3)/10=2.9次。在最坏的情况下，顺序查找的次数为10，而二分查找的次数为4。

### B+Tree

所以为了索引查找的高效性，我们引入了二叉查找树，然后MySQL是直接引入的B+树。

B+树索引就是传统意义上的索引，这是目前关系型数据库系统中查找最常用和最为有效的索引。B+树索引的构造类似于二叉树，根据键值（Key Value）快速找到数据。注意B+树中的B不是代表二叉(binary)，而是代表平衡(balance)，因为B+树是从最早的平衡二叉树演化而来，但是B+树不是一个二叉树。

网页工具：[Data Structure Visualization (usfca.edu)](https://www.cs.usfca.edu/~galles/visualization/Algorithms.html)

一棵m阶的B+树完整定义如下：

* 每个节点最多可以有 m 个元素；
* 除了根节点外，每个节点最少有 (m/2) 个元素；
* 如果根节点不是叶节点，那么它最少有 2 个孩子节点；
* 所有的叶子节点都在同一层；
* 非叶子节点只存放关键字和指向下一个孩子节点的索引，记录只存放在叶子节点中；
* 一个有 k 个孩子节点的非叶子节点有(k-1) 个元素，按升序排列；
* 某个元素的左子树中的元素都比它小，右子树的元素都大于或等于它（二叉排序树的特征）；
* 相邻的叶子节点之间用指针相连。

  以上知识可以不了解，一般面试不会问到B+树的细节（B+树的插入、B+树的删除、B+树的旋转等等），除非面试岗位就是做数据库实现的。
  如果想详细了解，可以找《算法导论》这本书

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/04f3f7da16024428af56e65fdadccce8.png)

  **面试问得比较多的可能是B树(也可以是B-树)、B*树，以及为什么选用B+树。**

  **MySQL中的B+Tree**

  1、MySQL中的B+树的叶子节点保存的是数据页，而不是单个数据。
  2、MySQL中实现的B+树，叶子节点之间的链表是双向链表，这是一个细微的差别。

  ![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/a07fdc0a59cb4d6799a57f601a533135.png)

  **那为什么MySQL不用B树而使用B+树呢？**
* 因为B数据每个节点都存储数据，每次查询的数据大小固定，就会造成每次查询返回的数据的条数变少，相同数据规模的情况下B树会增加io次数，而B+树，则数据量较小，一次可以返回多条记录，io次数较少
* 范围查询B+树明显优于B树

### MySQL中的索引

InnoDB存储引擎支持以下几种常见的索引：B+树索引、全文索引、哈希索引，其中比较关键的是B+树索引

在MySQL中如果你创建一个索引，就有一颗B+树。

比如A表有一个主键索引，那么就是一颗B+树，如果另外再创建一个索引a2，又会有一颗B+树。总之就是一张表最少会有一颗B+树，因为默认有主键，就算没有主键，也有隐藏的rowid创建出来的一颗主键索引的B+树。

#### 主键索引/聚簇索引/聚集索引

InnoDB中使用了聚集索引，就是将表的主键用来构造一棵B+树，并且将整张表的行记录数据存放在该B+树的叶子节点中。也就是所谓的索引即数据，数据即索引。由于聚集索引是利用表的主键构建的，所以每张表只能拥有一个聚集索引。

聚集索引的叶子节点就是数据页。换句话说，数据页上存放的是完整的每行记录。因此聚集索引的一个优点就是：通过过聚集索引能获取完整的整行数据。另一个优点是：对于主键的排序查找和范围查找速度非常快。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/7f328f91d2b14a4eb50d19187b9518ad.png)

如果我们没有定义主键呢？MySQL会使用唯一性索引，没有唯一性索引，MySQL也会创建一个隐含列RowID来做主键，然后用这个主键来建立聚集索引。

#### 普通索引/辅助索引/二级索引

聚簇索引只能在搜索条件是主键值时才能发挥作用，因为B+树中的数据都是按照主键进行排序的。

如果我们想以别的列作为搜索条件怎么办？我们一般会建立多个索引，这些索引被称为辅助索引/二级索引。

（每建立一个索引，就有一颗B+树,对于辅助索引(Secondary Index，也称二级索引、非聚集索引)，

叶子节点并不包含行记录的全部数据。叶子节点除了包含键值以外，每个叶子节点中的索引行中还包含了一个书签( bookmark)。该书签用来告诉InnoDB存储引擎哪里可以找到与索引相对应的行数据。因此InnoDB存储引擎的辅助索引的书签就是相应行数据的聚集索引键。

比如辅助索引index(node)，那么叶子节点中包含的数据就包括了(note和主键)。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/eb80ca41a76c44bdba04fed1f6ec42fc.png)

#### 联合索引/复合索引

前面我们对索引的描述，隐含了一个条件，那就是构建索引的字段只有一个，但实践工作中构建索引的完全可以是多个字段。所以，将表上的多个列组合起来进行索引我们称之为联合索引或者复合索引，比如index(a,b)就是将a,b两个列组合起来构成一个索引。

千万要注意一点，建立联合索引只会建立1棵B+树，多个列分别建立索引会分别以每个列则建立B+树，有几个列就有几个B+树，比如，index(note)、index(b)，就分别对note,b两个列各构建了一个索引。

而如果是index(note,b)在索引构建上，包含了两个意思：

1、先把各个记录按照note列进行排序。

2、在记录的note列相同的情况下，采用b列进行排序

从原理可知，为什么有最佳左前缀法则，就是这个道理

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1651212459071/6f15eac444b045179d65e601b588a03f.png)

## 高性能的索引创建实战

正确地创建和使用索引是实现高性能查询的基础。前面我们已经了解了索引相关的数据结构，各种类型的索引及其对应的优缺点。现在我们一起来看看如何真正地发挥这些索引的优势。

本课程以实际项目作为教学案例《飞滴出行网约车项目》

课程地址：https://www.mashibing.com/course/1537

课程资料：可以下载到对应的SQL。

### 高选择性索引优先

创建索引应该选择选择性/离散性高的列。索引的选择性/离散性是指，不重复的索引值（也称为基数，cardinality)和数据表的记录总数（N)的比值，范围从1/N到1之间。索引的选择性越高则查询效率越高，因为选择性高的索引可以让MySQL在查找时过滤掉更多的行。唯一索引的选择性是1，这是最好的索引选择性，性能也是最好的。

很差的索引选择性就是列中的数据重复度很高，比如性别字段，不考虑政治正确的情况下，只有两者可能，男或女。那么我们在查询时，即使使用这个索引，从概率的角度来说，依然可能查出一半的数据出来。

```
-- 计算司机手机号离散性
SELECT COUNT(DISTINCT driver_phone)/COUNT(*) FROM driver_user; 
-- 计算车牌号离散性
SELECT COUNT(DISTINCT vehicle_no)/COUNT(*) FROM car; 
```

以上就知道，手机号和车牌号是比较适合索引的。

### **三星索引概念**

对于一个查询而言，一个三星索引，可能是其最好的索引。

满足的条件如下：

* 索引将相关的记录放到一起则获得一星    （比重27%）
* 如果索引中的数据顺序和查找中的排列顺序一致则获得二星（排序星） （比重27%）
* 如果索引中的列包含了查询中需要的全部列则获得三星（宽索引星） （比重50%）

这三颗星，哪颗最重要？第三颗星。因为将一个列排除在索引之外可能会导致很多磁盘随机读（回表操作）。第一和第二颗星重要性差不多，可以理解为第三颗星比重是50%，第一颗星为27%，第二颗星为23%，所以在大部分的情况下，会先考虑第一颗星，但会根据业务情况调整这两颗星的优先度。

**一星：**

一星的意思就是：如果一个查询相关的索引行是相邻的或者至少相距足够靠近的话，必须扫描的索引片宽度就会缩至最短，也就是说，让索引片尽量变窄，也就是我们所说的索引的扫描范围越小越好。

**二星（排序星）** ：

在满足一星的情况下，当查询需要排序，group by、 order by，如果查询所需的顺序与索引是一致的（索引本身是有序的），是不是就可以不用再另外排序了，一般来说排序可是影响性能的关键因素。

**三星（宽索引星）** ：

在满足了二星的情况下，如果索引中所包含了这个查询所需的所有列（包括 where 子句和 select 子句中所需的列，也就是覆盖索引），这样一来，查询就不再需要回表了，减少了查询的步骤和IO请求次数，性能几乎可以提升一倍。

### 核心表索引设计

#### `order_info` 订单表

**业务场景** ：

* 按乘客/司机查询历史订单
* 按状态和时间范围查询
* 订单分页排序

**索引设计** ：

```
-- 三星索引：覆盖查询、排序、窄索引片
ALTER TABLE order_info
  ADD INDEX idx_passenger_time (passenger_id, order_time),  -- 乘客历史订单查询
  ADD INDEX idx_driver_status (driver_id, order_time,order_status)   -- 司机订单状态查询
  ADD INDEX idx_time_status (order_time, order_status);     -- 时间+状态联合查询
```

**三星评估** ：

1. ⭐ 窄索引片：通过passenger_id/driver_id缩小范围
2. ⭐ 排序：order_time满足时间排序需求
3. ⭐ 覆盖索引：包含查询所需字

#### `driver_user` 司机表

**业务场景** ：

* 按手机号快速登录
* 按状态和城市筛选

**索引设计** ：

```
ALTER TABLE driver_user
  ADD UNIQUE INDEX idx_phone (driver_phone),       -- 唯一索引用于登录
  ADD INDEX idx_state_city (state, address);       -- 状态+城市联合索引
```

#### `car` 车辆表

**业务场景** ：

* 按车牌号查询车辆信息
* 按状态和车型筛选

**索引设计** ：

```
ALTER TABLE car
  ADD UNIQUE INDEX idx_vehicle_no (vehicle_no),    -- 车牌号唯一索引
  ADD INDEX idx_state_type (state, vehicle_type);  -- 状态+车型联合索引
```

#### `driver_car_binding_relationship` 绑定关系表

**业务场景** ：

* 按司机查询绑定车辆
* 按车辆查绑定司机
* 绑定状态查询

**索引设计** ：

```
ALTER TABLE driver_car_binding_relationship
  ADD INDEX idx_driver (driver_id, bind_state),    -- 司机+状态
  ADD INDEX idx_car (car_id, bind_state);          -- 车辆+状态
```

### 联合索引顺序策略

`order_info` 时间范围查询优化

```
-- 低效索引（状态在前）
INDEX (order_status, order_time)

-- 高效索引（时间在前）
INDEX (order_time, order_status)
```

`order_time` 具有更高选择性（10万级唯一值），能更快缩小查询范围

### 只为用于搜索、排序或分组的列创建索引

业务分析。不是创建的索引越多越好，越多的索引（普通/联合） 都要创建一颗B+树，会降低插入的速度。
