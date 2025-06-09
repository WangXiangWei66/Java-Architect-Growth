//import javax.swing.table.TableCellEditor;
//import java.security.Key;
//import java.util.Map;
//
//public class HashMap {
//    public int MAXIMUM_CAPACITY = 10000;
////    public HashMap() {
////        //将默认的负载因子0.75赋值给load_factor，并没有创建数组
////        this.loadFactor = DEFAULT_LOAD_FACTOR;
////    }
////    //指定容量大小的构造函数 。
////    public HashMap(int initialCapacity) {
////        this(initialCapacity,DEFAULT_LOAD_FACTOR);
////    }
//
//
//
//
//
//
////    public HashMap(int initialCapacity,float loadFactor) {
////        //判断初始化容量是否小于零
////        if (initialCapacity < 0) {
////            throw new IllegalArgumentException("illegal initial capacity:" + initialCapacity);
////        }
////        // 判断初始化容量initialCapacity是否大于集合的最大容量MAXIMUM_CAPACITY
////        if (initialCapacity > MAXIMUM_CAPACITY) {
////            initialCapacity = MAXIMUM_CAPACITY;
////        }
////        // 判断负载因子loadFactor是否小于等于0或者是否是一个非数值
////        if (loadFactor <= 0 || Float.isNaN(loadFactor))
////            throw new IllegalArgumentException("illegal load factor :" + loadFactor);
////        // 将指定的负载因子赋值给HashMap成员变量的负载因子loadFactor
////        this.loadFactor = loadFactor;
////        this.threshold = tableSizeFor(initialCapacity);
////    }
////    //返回比指定初始化容量大的最小的2的n次幂
////    static final int tableSizeFor(int cap) {
////        int n = cap - 1;
////        n |= n>>>1;//无符号右移，高位补0
////        n |= n>>>2;
////        n |= n>>>4;
////        n |= n>>>8;
////        n |= n>>>16;
////        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY: n + 1;
////
////    }
//
//
//
//
//
//
//
//    //构造一个映射关系与指定map相同的新HashMap
//    public HashMap(Map<? extends K,? extends V>m) {
//        this.loadFactor = DEFAULT_LOAD_FACTOR;
//        putMapEntries(m,false);
//    }
//
//    final void putMapEntries(Map<? extends K,? extends V>m,boolean evict){
//        int s = m.size();
//        if (s > 0) {
//            if (table == null) {
//               float ft = ((float)s /loadFactor) + 1.0F;
//                int t = ((ft < (float)MAXIMUM_CAPACITY) ? (int)ft : MAXIMUM_CAPACITY);
//                // 计算得到的t大于阈值，则初始化阈值
//                if (t > threshold)
//                    threshold = tableSizeFor(t);
//            }
//            // 已初始化，并且m元素个数大于阈值，进行扩容处理
//            else if (s > threshold)
//                resize();
//            // 将m中的所有元素添加至HashMap中
//            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
//                K key = e.getKey();
//                V value = e.getValue();
//                putVal(hash(key), key, value, false, evict);
//            }
//        }
//    }
//}
