# 1、连接查询

## 连接查询的本质

**连接的本质就是把各个连接表中的记录都取出来依次匹配的组合加入结果集并返回给用户。**

比如sql：SELECT * FROM e1, e2;

我们把e1和e2两个表连接起来的过程如下图所示：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1655442554057/ce63857198984eee994ffcf826ab953e.png)

这个过程看起来就是把e1表的记录和e2的记录连起来组成新的更大的记录，所以这个查询过程称之为连接查询。连接查询的结果集中包含一个表中的每一条记录与另一个表中的每一条记录相互匹配的组合，像这样的结果集就可以称之为 **笛卡尔积** 。

因为表e1中有3条记录，表e2中也有3条记录，所以这两个表连接之后的笛卡尔积就有3×3=9行记录。

我们可以连接任意数量张表，但是如果没有任何限制条件的话，这些表连接起来产生的笛卡尔积可能是非常巨大的。比方说3个100行记录的表连接起来产生的笛卡尔积就有100×100×100=1000000行数据！

### 连接过程

这个查询语句：

t1表：![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/9f2618d6fb0045dfb954fde1ad5d98ed.png)

t2表：![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/a36d92a19cf543a88438546ceeacd6fa.png)

```
SELECT * FROM t1, t2 WHERE t1.m1 > 1 AND t1.m1 = t2.m2 AND t2.n2 < 'd';
```

1、首先确定第一个需要查询的表，这个表称之为 **驱动表（假设这里确定t1为驱动表）**

2、遍历驱动表（t1）的结果，到被驱动表(t2)中查找匹配记录

所以整个连接查询的执行过程就如下图所示：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1655442554057/b4edbeb22ba44282a683e282d6d6d386.png)

也就是说整个连接查询最后的结果只有两条符合过滤条件的记录：

从上边两个步骤可以看出来，这个两表连接查询共需要查询1次t1表，2次t2表。

也就是说在两表连接查询中， **驱动表只需要访问一次，被驱动表可能被访问多次** 。

### 执行计划分析

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/c30364a2afd54602b36558191bdc934c.png)

这里发现驱动表是t2。

1、首先确定第一个需要查询的表，这个表称之为 **驱动表（假设这里确定t2为驱动表）**

2、遍历驱动表（t2）的结果，到被驱动表(t1)中查找匹配记录

### 多表连接

在MySQL中，多表连接（Multi-table Join）是2表连接的扩展，涉及3个或更多表的关联查询。其核心原理是通过连接条件将多个表的数据按行组合。

对于两表连接，比如表A和表B连接只有 AB、BA这两种连接顺序。其实相当于2× 1 = 2种连接顺序。

对于三表连接，比如表A、表B、表C进行连接有ABC、ACB、BAC、BCA、CAB、CBA这么6种连接顺序。其实相当于3 × 2 × 1 = 6种连接顺序。

对于四表连接的话，则会有4 × 3 × 2 × 1 = 24种连接顺序。对于n表连接的话，则有 n × (n-1) × (n-2) × ··· × 1种连接顺序，就是n的阶乘种连接顺序，也就是n!。

## SQL中的Join操作

以下分析的是是连接查询的两个表的关系，不是上面的笛卡尔积。

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/8472f7942c5942c7a95edf5fde253223.png)

1）左连接LEFT JOIN

左连接是将左表作为主表，右表作为从表。左表中的所有记录作为外层循环，在右表中进行匹配，如果右表中没有匹配的行，则将左表记录的右表项补空值。对应的SQL语句如下：

```
SELECT * FROM TableA a LEFT JOIN TableB b ON a.KEY = b.KEY
```

2）左外连接LEFT OUTER JOIN

左外连接是将左表作为主表，右表作为从表，循环遍历右表，查找与条件满足的项，如果在右表中没有匹配的项，则补空值，并且在结果集中选择只在左表中存在的数据。对应的SQL语句如下：

```
SELECT * FROM TableA a LEFT JOIN TableB b ON a.KEY = b.KEY WHERE b.KEY IS NULL
```

3）右连接RIGHT JOIN

右连接是将右表作为主表，左表作为从表。右表中的所有记录作为外层循环，在左表中进行匹配，如果左表中没有匹配的行，则将右表记录的左表项补空值。对应的SQL语句如下：

```
SELECT * FROM TableA a RIGHT JOIN TableB b ON a.KEY = b.KEY
```

4）右外连接RIGHT OUTER JOIN

右外连接选择将右表作为主表，左表作为从表，循环遍历左表，查找与join条件满足的项，如果在左表中没有匹配的项，则补空值，并且在结果集中选择只在右表中存在的数据。对应的SQL语句如下：

```
SELECT * FROM TableA a RIGHT JOIN TableB b ON a.KEY = b.KEY WHERE a.KEY IS NULL
```

5）内连接INNER JOIN

内连接是将左表和右表对于条件相匹配的项进行组合，返回相关列值相等的结果。对应的SQL语句如下：

```
SELECT * FROM TableA a INNER JOIN TableB b ON a.KEY = b.KEY
```

6）全连接FULL JOIN

全连接是将左表和右表的所有记录进行匹配，如果在另外表项中不存在记录，则补空值。对应的SQL语句如下：

```
SELECT * FROM TableA a FULL OUTER JOIN TableB b ON a.KEY = b.KEY
```

7）全外连接FULL OUTER JOIN

全外连接是将全连接中表相交的部分去除掉。对应的SQL语句如下：

```
SELECT * FROM TableA a FULL OUTER JOIN TableB b ON a.KEY = b.KEY WHERE a.KEY IS NULL OR b.KEY IS NULL
```

### SQL JOIN案例实战

```
-- 创建部门表
CREATE TABLE departments (
    dept_id INT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL
);

-- 创建员工表
CREATE TABLE employees (
    emp_id INT PRIMARY KEY,
    emp_name VARCHAR(50) NOT NULL,
    dept_id INT,
    salary DECIMAL(10, 2)
);

-- 插入部门数据
INSERT INTO departments (dept_id, dept_name) VALUES
(1, '技术部'),
(2, '市场部'),
(3, '财务部'),
(4, '人力资源部'),
(5, '销售部');

-- 插入员工数据
INSERT INTO employees (emp_id, emp_name, dept_id, salary) VALUES
(101, '张三', 1, 8500.00),
(102, '李四', 1, 9200.00),
(103, '王五', 2, 7800.00),
(104, '赵六', 3, 9500.00),
(105, '钱七', 3, 8800.00),
(106, '孙八', NULL, 7500.00),
(107, '周九', 5, 8200.00),
(108, '吴十', 5, 9000.00),
(109, '郑十一', 2, 8700.00),
(110, '王十二', NULL, 8000.00);
```

#### 1. 内连接 (INNER JOIN)

返回两个表中匹配的记录，结果仅包含匹配的记录

```
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
INNER JOIN departments d ON e.dept_id = d.dept_id;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/09ceddeded9a4b95b6669edcc3a25ee1.png)

#### **2. 左连接 (LEFT JOIN)**

返回左表所有记录 + 右表匹配的记录，数据结果左表完整，右表可能为NULL

```
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/5e5fb1756b3940a0a5a5e723466e1b2b.png)

#### **3. 左外连接 (LEFT OUTER JOIN)**

返回左表有但右表没有匹配的记录，仅左表有值，右表为NULL

```
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE d.dept_id IS NULL;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/9ba519c5ccf3484c8c74768d185318eb.png)

#### 4. 右连接 (RIGHT JOIN)

```
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
```

返回右表所有记录 + 左表匹配的记录，结果右表完整，左表可能为NULL

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/59fe778c48ec4051a9a2fc708d3d8d5a.png)

#### 5. 右外连接 (RIGHT OUTER JOIN)

返回右表有但左表没有匹配的记录，结果仅右表有值，左表为NULL

```
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id
WHERE e.dept_id IS NULL;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/232ea63197614bbf89b87f1cd3b9cea4.png)

#### 6. 全连接 (FULL JOIN)

返回两个表所有记录，结果所有记录，不匹配部分为NULL

```

SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
UNION
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/303e894489224dfc9aa05c0cda880b1c.png)

#### 7. 全外连接 (FULL OUTER JOIN)

返回两个表没有匹配的记录，结果仅不匹配的记录

```

SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.dept_id
WHERE d.dept_id IS NULL
UNION
SELECT 
    e.emp_id, e.emp_name, d.dept_name, e.salary
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.dept_id
WHERE e.dept_id IS NULL;
```

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/91dfeaf4142246cb93108fe7ad5bfba3.png)

## MySQL中Join算法

mysql在多表查询的时候，可以使用多种join算法，比如 Nested Loop Join（嵌套循环连接）、Index Nested-Loop Join（索引嵌套循环连接）、Block Nested-Loop Join（块嵌套循环连接）和Hash Join（哈希连接）。

在MySQL 5.5版本之前，只支持一种关联算法Nested Loop Join，在5.5版本后通过引入Index Nested-Loop Join和Block Nested-Loop Join算法来优化嵌套查询。从MySQL 8.0.18开始，MySQL实现了对于相等条件下的Hash Join，并且join条件中无法使用任何索引。相对于Blocked Nested Loop算法，hash join性能更高，并且两者的使用场景相同，所以从8.0.20开始，Blocked Nested Loop算法已经被移除，使用hash join替代之。

### 1、Nested Loop Join

Nested Loop Join（NLJ）本质上是一个双层for循环，对于外表中的每一行数据，MySQL检查内表中是否满足JOIN条件。如果满足，则将其添加到结果集中。

**外层表/外表：驱动表、内层表/内表：被驱动表**

Nested Loop Join的执行流程如下图所示：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/3d9f0c2061504d1e83e546535152654b.png)

1、SQL从外表T2中读取一行记录，取出关联字段id到内表T1中逐条查找；

2、取出T1表中满足条件的记录与T2表中获取的结果进行合并，并将结果放入结果集中；

3、循环上述过程直到无法满足条件，将结果返回给客户端。Nested Loop Join伪代码实现如下：

```
for each row in t1 matching range {
 for each row in t2 matching reference key {
   if(t1.id==t2.id) {
  //返回结果
   }
  }
 }
```

Nested Loop Join算法在处理小数据集时可能非常有效，但是对于大型数据集，可能会导致性能下降。通过双层循环来进行比较值获取结果，就是对外表和内表进行笛卡尔积运算，比如t1和t2表的数据量分别为R和S，运算的成本为O(R*S)，当表数据量大时候，执行效率会非常低。

比如：如果两个表各有100万行，那么NLJ需要执行100万*100万=1万亿次比较操作。这显然是不可接受的。

如果内表无法完全放入内存，那么对于外表的每一行，都需要从磁盘中读取内表的一部分数据（即使有索引，也可能需要随机I/O）。这将导致大量的磁盘I/O操作，而磁盘I/O通常是数据库操作中最慢的部分

### 2、Index Nested-Loop Join

Index Nested-Loop Join（INLJ）是Nested-Loop Join的改进版，其优化思路是通过索引访问减少内层循环的匹配次数，也就是通过外层数据与内存的索引数据进行循环匹配，以减少数据访问提高查询效率。INLJ执行过程如下所示：

![](file://D:/%E8%AF%BE%E7%A8%8B/Java%E4%BC%81%E4%B8%9A%E7%BA%A7%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/2025%E5%B9%B4%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/MySQL/MySQL8/image/1703411775247.png?lastModify=1752042541)![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/8fbf55257e25486bb66a76162d7a0d5f.png)

1、SQL从外表T2中读取一行记录，取出关联字段id到内表T1的索引树中查找；

2、从T1表中取出辅助索引树中满足条件的记录查到主键ID，再到主键索引中根据主键ID将剩下字段的数据取出与T2中获取到的结果进行合并，并将结果放入结果集

3、循环上述过程直到无法满足条件，将结果返回给客户端。

Index Nested-Loop Join被很多人诟病效率不高，主要是因为在Join过程很多时候用到的不是主键的cluster index而是辅助索引。

如果关联字段id在T1表的主键索引字段中，则直接通过主键索引获取到数据，索引查找的开销非常小，并且访问模式也是顺序的；如果关联字段在辅助索引字段中，如果查询需要访问聚集索引上的列，那么必要需要进行回表取数据。辅助索引是随机IO访问、再回表查询又是随机IO访问，因此执行效率会降低。

### 3、Block Nested-Loop Join

Block Nested-Loop Join（BNLJ）也是Nested-Loop Join的优化方法，如果Join的关联字段不是索引或者有一个字段不在索引中，则会采用该算法进行查询。在BNLJ算法中增加一个join_buffer缓存块，在Join操作时候会把外表的数据放入缓存块中，然后扫描内表，把内表每一行取出来跟join_buffer中的数据批量做对比。BNLJ执行过程如下所示：

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/4a9a0299276c404f9fe92c5e9caf94fd.png)

![](file://D:/%E8%AF%BE%E7%A8%8B/Java%E4%BC%81%E4%B8%9A%E7%BA%A7%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/2025%E5%B9%B4%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/MySQL/MySQL8/image/1703411887273.png?lastModify=1752042541)

1、将外表T2中的数据读入到join_buffer中（默认内存大小为256k,如果数据量多,会进行分段存放,然后进行比较）

2、把表T1的每一行数据，跟join_buffer中的数据批量进行对比，匹配的数据与T2表中获取的结果进行合并，并将结果放入结果集中；

3、循环以上步骤直到无法满足条件，将结果集返回给客户端

Block Nested-Loop Join的优化思路是利用join_buffer减少外表的循环次数，通过一次性缓存多条记录数，将参与查询的列放入join_buffer中，然后拿join buffer里的数据批量与内层表的数据进行匹配，从而减少对外表的访问IO次数。

在MySQL 8.0.18版本之前，不使用Index Nested-Loop Join的时候，默认使用的是Block Nested-Loop Join。在8.0.20版本以后，MySQL中不再使用Block Nested-Loop Join，由hash join进行替代优化。

```

Prior to MySQL 8.0.18, this algorithm was applied for equi-joins when no indexes could be used; in MySQL 8.0.18 and later, the hash join optimization is employed in such cases. Starting with MySQL 8.0.20, the block nested loop is no longer used by MySQL, and a hash join is employed for in all cases where the block nested loop was used previously.
```

使用Block Nested-Loop Join算法需要开启优化器管理配置的optimizer_switch的设置block_nested_loop为on，默认为开启。

1）查看block_nested_loop配置

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/9cb3de693c5645daa2a9d502c7acbfce.png)

2）查看join_buffer_size参数配置

![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/966d3664919640768f7ab2ed22f255a6.png)

Join buffer的大小由参数join_buffer_size控制，默认是256KB，对于一些复杂的SQL语句为了提升性能可以调整该参数值。

需要注意的是变量 join_buffer_size 的最大值在MySQL 5.1.22 版本前是4G，而之后的版本才能在64位操作系统下申请大于4G的Join Buffer空间。

在MySQL中Join buffer还有以下特性：

1、Join Buffer的使用条件：Join Buffer只有在join类型为all、index、range的时候才可以使用。也就是join字段没有使用到索引或部分字段不在索引列中会用到。

2、Join Buffer的分配与释放：在join之前就会分配Join Buffer，每个join都会分配一个buffer。在查询执行完毕后就会释放Join Buffer。

3、Join Buffer中保存的数据：Join Buffer中只会保存参与join的列，并非整个数据行。即使通过调整参数使其变大，也并不会加速查询，因为其内部逻辑和数据的排布方式不会因此而改变。

由于每次Join都会分配一个Join Buffer，假设高并发P查询有N张表参与Join，每张表之间使用Block Nested-Loop Join算法，需要分配P*(N-1)个Join buffer。因此Join Buffer的设置需要考量，设置不当有可能会引起内存分配不足导致数据库宕机。

### 4、Hash Join

MySQL 8.0.18版本开始增加了对Hash Join算法的支持，以提升Join的性能，在8.0.20版本以后，MySQL中不再使用Block Nested-Loop Join，由hash join进行替代优化。

在MySQL 8中hash join的使用前提条件是表与表之间是等值连接并且连接字段上不使用索引，或者是不包含任何连接条件的笛卡尔连接，否则hash join会退化。

为了支持hash join，mysql在优化器optimizer_switch中新增了hash join开关选项hash_join，默认是ON状态。同时新增了两个hint：HASH_JOIN和NO_HASH_JOIN，用于在SQL级别控制hash join行为。

**但是从MySQL 8.0.19开始，这两个hint被置为无效，hash join的使用就不受用户控制，由优化器决定。**

并且在MySQL 8.0.20及更高版本中，取消了对等值条件的约束，可以全面支持non-equi-join，Semijoin，Antijoin，Left outer join/Right outer join。比如非等值join使用hash join算法：

```
mysql> EXPLAIN FORMAT=TREE SELECT * FROM t1 JOIN t2 ON t1.c1 < t2.c1\G
*************************** 1. row ***************************
EXPLAIN: -> Filter: (t1.c1 < t2.c1)  (cost=4.70 rows=12)
    -> Inner hash join (no condition)  (cost=4.70 rows=12)
        -> Table scan on t2  (cost=0.08 rows=6)
        -> Hash
            -> Table scan on t1  (cost=0.85 rows=6)

```

Hash join的基本原理是通过Hash的方式降低复杂度，MySQL根据连接条件对外表建立Hash表，对于内表的每一行记录也根据连接条件计算Hash值，只需要验证对应的hash值能否匹配完成连接操作。但是如果外表过大或者hash join可使用的内存过小，外表数据不能全部加载到内存中，优化器会把外表切分为不同的partition，使得切分后的分片能够放入内存，不能放入内存的会写入磁盘的chunk files中。

#### 5.1 In-memory Hash-join

![](file://D:/%E8%AF%BE%E7%A8%8B/Java%E4%BC%81%E4%B8%9A%E7%BA%A7%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/2025%E5%B9%B4%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/MySQL/MySQL8/image/1703412428650.png?lastModify=1752042541)![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/f0c511bfb5314d4f90b671985aa823fb.png)

外表数据能够全部放入内存中，称为in-memory hash-join，hash join分为两个过程：build过程构建hash表和probe过程探测hash表。

1、Build过程：遍历外表，以连接条件”countries.country_id”为hash key，查询需要的列作为value创建hash表。通常优化器优先选择占用内存最小的表作为外表构建hash表。

2、Probe过程：逐行遍历内表，对于内表的每行记录，根据连接条件”persons.country_id”计算hash值，并在hash表中查找。如果匹配到外表的记录，则输出，否则跳过，直到遍历完成所有内表的记录

上述场景适用于表数据能够存放在内存中的场景，这个内存由参数join_buffer_size控制，并且可以动态调整生效。

#### 5.2 On-disk Hash-join

在build阶段如果内存不够，优化器会将外表分成若干个partition执行，这些partition是保存在磁盘上的chunks。优化器会根据内存的大小计算出合适的chunks数，但是在mysql中chunk file数目硬限制为128个。分片的过程如下图所示：

![](file://D:/%E8%AF%BE%E7%A8%8B/Java%E4%BC%81%E4%B8%9A%E7%BA%A7%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/2025%E5%B9%B4%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/MySQL/MySQL8/image/1703412520949.png?lastModify=1752042541)![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/dbc632cd76174422b7cf4219043472cd.png)

在build阶段优化器根据hash算法将外表数据存放到磁盘中对应的chunks文件中，在probe阶段对内表数据使用同样的hash算法进行分区并存放在磁盘的chunks文件中。由于使用相同的hash函数，那么key相同（join条件相同）必然在同一个分片编号的chunk文件中。接下来，再对外表和内表中相同分片编号的数据放入到内存中进行Hash Join计算，所有分片计算完成后，整个join过程结束。这种算法的代价是外表和内表在build阶段进行一次读IO和一次写IO，在probe阶段进行了一次读IO。整个过程如下图所示：

![](file://D:/%E8%AF%BE%E7%A8%8B/Java%E4%BC%81%E4%B8%9A%E7%BA%A7%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/2025%E5%B9%B4%E6%9E%B6%E6%9E%84%E5%B8%88%E6%88%90%E9%95%BF%E7%8F%AD/MySQL/MySQL8/image/1703412549761.png?lastModify=1752042541)![image.png](https://fynotefile.oss-cn-zhangjiakou.aliyuncs.com/fynote/fyfile/5983/1752041001081/3441e669ab864d8ea02865e69354b7ab.png)

上述算法能够解决内存不足的Join问题，但是如果数据倾斜严重导致哈希后的分片仍然超过内存的大小，MySQL优化器的处理方法是：读满内存中的hash表后停止build过程，然后执行一次probe。待处理这批数据后，清空hash表，在上次build停止的位点继续build过程来填充hash表，填充满再做一趟内表分片完整的probe。直到处理完所有build数据。

### 6、不同Join的性能测试

#### 6.1、环境准备

```

mysql> select version();
+-----------+
| version() |
+-----------+
| 8.2.0     |
+-----------+
1 row in set (0.00 sec)

mysql> show variables like 'join_buffer_size';
+------------------+--------+
| Variable_name    | Value  |
+------------------+--------+
| join_buffer_size | 262144 |
+------------------+--------+
1 row in set (0.00 sec)

```

#### 6.2、准备数据

```

##创建表
CREATE TABLE `t1` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `c1` int(11) DEFAULT NULL,
  `c2` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

mysql> create table t2 like t1;

##插入数据到表中
-- 往t1表插入5万行记录
drop procedure if exists insert_t1; 
delimiter ;;
create procedure insert_t1()        
begin
  declare i int;                    
  set i=1;                          
  while(i<=50000)do                 
    INSERT INTO t1 (c1, c2) VALUES (RAND() * 100000, CONCAT('Record ', i));
    set i=i+1;                       
  end while;
end;;
delimiter ;
call insert_t1();

-- 往t2表插入1000行记录
drop procedure if exists insert_t2; 
delimiter ;;
create procedure insert_t2()        
begin
  declare i int;                    
  set i=1;                          
  while(i<=1000)do                 
    INSERT INTO t2 (c1, c2) VALUES (RAND() * 100, CONCAT('Record ', i));
    set i=i+1;                       
  end while;
end;;
delimiter ;
call insert_t2();

```

#### 6.3、使用hash Join

```

mysql> explain format=tree select * from t1 join t2 on t1.c1=t2.c1\G
*************************** 1. row ***************************
EXPLAIN: -> Inner hash join (t1.c1 = t2.c1)  (cost=4.97e+6 rows=4.97e+6)
    -> Table scan on t1  (cost=0.677 rows=49655)
    -> Hash
        -> Table scan on t2  (cost=101 rows=1000)

1 row in set (0.00 sec)

```

#### 6.4、向t1条添加索引，使用nested loop inner join

```

mysql> create index idx_c1 on t2(c1);
Query OK, 0 rows affected (0.04 sec)
Records: 0  Duplicates: 0  Warnings: 0

mysql> explain format=tree select * from t1 join t2 on t1.c1=t2.c1\G
*************************** 1. row ***************************
EXPLAIN: -> Nested loop inner join  (cost=177078 rows=491634)
    -> Filter: (t1.c1 is not null)  (cost=5006 rows=49655)
        -> Table scan on t1  (cost=5006 rows=49655)
    -> Index lookup on t2 using idx_c1 (c1=t1.c1)  (cost=2.48 rows=9.9)

1 row in set (0.00 sec)

mysql> alter table t2 drop index idx_c1;
Query OK, 0 rows affected (0.00 sec)
Records: 0  Duplicates: 0  Warnings: 0

```

#### 6.5、使用hint(BNL和NO_BNL)将hash join 启动和关闭

```

mysql> explain format=tree select /*+ NO_BNL(t1,t2)*/ * from t1 join t2 on t1.c1=t2.c1\G
*************************** 1. row ***************************
EXPLAIN: -> Nested loop inner join  (cost=5.01e+6 rows=4.97e+6)
    -> Table scan on t2  (cost=101 rows=1000)
    -> Filter: (t1.c1 = t2.c1)  (cost=40.7 rows=4966)
        -> Table scan on t1  (cost=40.7 rows=49655)

1 row in set (0.01 sec)

```
