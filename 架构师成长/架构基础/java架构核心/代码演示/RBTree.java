//红黑树类结构的定义
public class RBTree {

    //定义颜色常量
    //static关键字是静态变量
    //final表示不可变
    private static final boolean RED = false;
    private static final boolean BLACK = true;

    //定义根节点
    private static RBNode root;

    //获取根节点
    public RBNode getRoot() {
        return root;
    }

    //设置根节点
    public void setRoot(RBNode root) {
        this.root = root;
    }

    //红黑树节点类定义，使用泛型，键K实现Comparable接口以便比较大小
    static class RBNode<K extends Comparable<K>, V> {
        private RBNode parent;
        private RBNode left;
        private RBNode right;
        private boolean color;//节点颜色
        private K key;
        private V value;
        private RBNode root;


        public RBNode(RBNode parent, RBNode left, RBNode right, boolean color, K key, V value) {
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.color = color;
            this.key = key;
            this.value = value;
        }

        public RBNode(K key, RBNode left, Object right) {

        }

        public RBNode getParent() {
            return parent;
        }

        public void setParent(RBNode parent) {
            this.parent = parent;
        }

        public RBNode getLeft() {
            return left;
        }

        public void setLeft(RBNode left) {
            this.left = left;
        }

        public RBNode getRight() {
            return right;
        }

        public void setRight(RBNode right) {
            this.right = right;
        }

        public boolean isColor() {
            return color;
        }

        public void setColor(boolean color) {
            this.color = color;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        /**
         * 围绕p左旋
         * p               pr(r)
         * / |             / \
         * pl  pr(r) =>    p   rr
         * / \         / \
         * rl  rr     pl  rl
         * <p>
         * 左旋的时候
         * p-pl 和 pr-rr的关系不变
         * pr-rl 要变为 p-rl
         * 也就是 rl要变为 p的右子节点
         * 同时 p要成为 rl 的父节点
         * 还有就是要判断  p 是否有父节点
         * 如果没有
         * r 变为 root 节点
         * 如果有
         * r.parent = p.parent
         * 还要设置 r为 p.parent 的子节点(可能左也可能右)
         * 如果 p.parent.left == p
         * p.parent.left = r;
         * 否则
         * p.parent.right = r;
         * 最后
         * p.parent = r;
         * r.left = p;
         *
         * @param p
         */
        private void leftRotate(RBNode p) {
            if (p != null) {
                //保存p的右子节点r
                RBNode r = p.right;
                //将r的左子树移动到p的右子树位置
                p.right = r.left;
                //p的右节点变成了r的左节点
                if (r.left != null) {
                    r.left.parent = p;
                }
                //将r的父节点设成p的父节点
                r.parent = p.parent;
                if (p.parent == null) {
                    root = r;
                } else if (p.parent.left == p) {
                    p.parent.left = r;
                } else {
                    p.parent.right = r;
                }
                r.left = p;
                p.parent = r;
            }
        }

        public void rightRotate(RBNode p) {
            if (p != null) {
                //保存p的左子节点
                RBNode r = p.left;
                //将r的右子节点移动到p的左子节点位置
                p.left = r.right;
                if (r.right != null) {
                    r.right.parent = p;
                }
                r.parent = p.parent;
                if (p.parent == null) {
                    root = r;
                } else if (p.parent.left == p) {
                    p.parent.left = r;
                } else {
                    p.parent.right = r;
                }
                r.right = p;
                p.parent = r;
            }
        }

        //put是向红黑树中插入新节点
        public void put(K key, V value) {
            //获取当前根节点
            RBNode t = this.root;
            if (t == null) {
                root = new RBNode<>(null, null, null, BLACK, key, value);
                return;
            }
            int cmp;//键比较的结果
            RBNode parent;//当前节点的父节点
            //不允许键为null，红黑树要求键可以比较，null无法调用compareTo方法
            if (key == null) {
                throw new NullPointerException();
            }
            //查找插入的位置，循环直到找到空节点
            do {
                //记录当前节点的父节点
                parent = t;
                //比较当前键和节点键
                cmp = key.compareTo((K) t.key);
                if (cmp < 0) {
                    //<0插入左子树
                    t = t.left;
                } else if (cmp > 0) {
                    //>0插入右子树
                    t = t.right;
                } else {
                    //根据键是否存在，判断是更新键还是更新值
                    t.setValue(value == null ? key : value);
                    return;
                }
            } while (t != null);
            //创建新节点，颜色先设为红色，红黑树的默认
            RBNode<K, V> e = new RBNode<>(parent, null, null, RED, key, value);
            if (cmp < 0) {
                parent.left = e;
            } else {
                parent.right = e;
            }
            //插入后，修复红黑树的性质
            fixAfterPut((RBNode<K, Object>) e);
        }

        //根据红黑树的节点进行调整
        private boolean colorOf(RBNode node) {
            //返回节点的颜色，空节点视为黑色，这是红黑树的性质决定，叶子节点默认是黑色
            return node == null ? BLACK : node.color;
        }

        private RBNode parentOf(RBNode node) {
            //获取父节点，空节点返回空
            return node != null ? node.parent : null;
        }

        private RBNode leftOf(RBNode node) {
            return node != null ? node.left : null;
        }

        private RBNode rightOf(RBNode node) {
            return node != null ? node.right : null;
        }

        private void setColor(RBNode node, boolean color) {
            if (node != null) {
                //安全设置节点的颜色，避免出现空指针
                node.setColor(color);
            }
        }

        //红色节点的子节点不能是红色（新节点为红色，若父节点也是红色则违反）
        private void fixAfterPut(RBNode<K, Object> x) {
            x.color = RED;//新节点默认是红色，（可能出发修复）
            //如果当前节点不是根节点，且父节点是红色节点，则会出发修复条件
            while (x != null && x != root && x.parent.color == RED) {
                //如果父节点是祖父节点的左节点
                if (parentOf(x) == parentOf(parentOf(x)).left) {
                    //y为x的叔叔节点
                    RBNode y = rightOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {//如果叔叔节点是红色
                        setColor(parentOf(x), BLACK);//x的父节点变成黑色
                        setColor(y, BLACK);//叔叔节点也变成黑色
                        setColor(parentOf(parentOf(x)), RED);//祖父节点变成红色
                        x = parentOf(parentOf(x));
                    } else {//叔叔节点为黑色
                        if (x == parentOf(x).right) {
                            //当前节点是父节点的右，则左旋
                            x = parentOf(x);
                            leftRotate(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        rightRotate(parentOf(parentOf(x)));
                    }
                } else {//父节点是祖父节点的右子节点
                    RBNode y = leftOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {
                        setColor(parentOf(x), BLACK);
                        setColor(y, BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == parentOf(x).left) {
                            x = parentOf(x);
                            rightRotate(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        leftRotate(parentOf(parentOf(x)));
                    }
                }
            }
            root.color = BLACK;
        }

        private RBNode getNode(K key) {
            RBNode node = this.root;
            while (node != null) {
                int cmp = key.compareTo((K) node.key);
                if (cmp < 0) {
                    node = node.left;
                } else if (cmp > 0) {
                    node = node.right;
                } else {
                    return node;
                }
            }
            return null;
        }

        public V remove(K key) {
            RBNode node = getNode(key);
            if (node == null) {
                return null;
            }
            //把值存起来，删除后返回
            V oldValue = (V) node.value;
            deleteNode(node);
            return oldValue;
        }

        private void deleteNode(RBNode node) {
            //有两个节点的情况
            if (node.left != null && node.right != null) {
                //找后继节点
                RBNode successor = successor(node);
                node.key = successor.key;
                node.value = successor.value;
                //将删除的节点变成后继节点
                node = successor;
            }
            //有一个节点的情况
            RBNode replacement = node.left != null ? node.left : node.right;
            if (replacement != null) {
                //// 替代者的父指针指向原来 node 的父节点
                replacement.parent = node.parent;
                if (node.parent == null) {
                    root = replacement;
                } else if (node == node.parent.left) {
                    node.parent.left = replacement;
                } else {
                    node.parent.right = replacement;
                }
                //将node的左右孩子和父指针都指向null，node等待GC
                node.left = node.right = node.parent = null;
                if (node.color == BLACK) {
                    //替换完成后，调整平衡
                    fixAfterPut(replacement);
                }
            } else if (node.parent == null) {
                root = null;
            } else {
                //node节点为叶子节点，replacement为空
                if (node.color == BLACK) {
                    fixAfterPut(node);
                }
                if (node.parent != null) {
                    if (node == node.parent.left) {
                        node.parent.left = null;
                    } else {
                        node.parent.right = null;
                    }
                    node = null;
                }
            }
        }

        private void fixAfterRemove(RBNode x) {
            //如果x不是根节点，并且颜色是黑色
            while (x != root && colorOf(x) == BLACK) {
                //如果x是父亲节点的左孩子
                if (x == leftOf(parentOf(x))) {
                    //获取兄弟节点
                    RBNode rNode = rightOf(parentOf(x));
                    //如果兄弟节点是红色
                    if (colorOf(rNode) == RED) {
                        //将他涂黑
                        setColor(rNode, BLACK);
                        //将x的父涂红
                        setColor(parentOf(x), RED);
                        //对x的父左旋
                        leftRotate(parentOf(x));
                        //更新兄弟节点，左旋后兄弟节点是原兄弟节点的左子节点
                        rNode = rightOf(parentOf(x));
                    }
                    if (colorOf(leftOf(rNode)) == BLACK && colorOf(rightOf(rNode)) == BLACK) {
                        setColor(rNode, RED);
                        //向上回溯，处理父节点
                        x = parentOf(x);
                    } else {
                        //兄弟节点至少有一个红色
                        if (colorOf(rightOf(rNode)) == BLACK) {
                            setColor(rNode, RED);//兄弟节点涂红
                            setColor(leftOf(rNode), BLACK);
                            rightRotate(rNode);
                            rNode = rightOf(parentOf(x));
                        }
                        //兄弟节点的右子节点是红色，则进行统一处理
                        setColor(rNode, colorOf(parentOf(x)));//兄弟节点的颜色，设为父节点的颜色
                        setColor(parentOf(x), BLACK);
                        setColor(rightOf(rNode), BLACK);
                        leftRotate(parentOf(x));
                        x = root;//修复完成，跳出循环
                    }
                } else {
                    //x是父节点的右子节点的情况
                    RBNode rbNode = leftOf(parentOf(x));
                    if (colorOf(rbNode) == RED) {
                        setColor(rbNode, BLACK);
                        setColor(parentOf(x), RED);
                        rightRotate(parentOf(x));
                        rbNode = leftOf(parentOf(x));
                    }

                    if (colorOf(rightOf(rbNode)) == BLACK && colorOf(leftOf(rbNode)) == BLACK) {
                        setColor(rbNode, RED);
                        x = parentOf(x);
                    } else {
                        if (colorOf(leftOf(rbNode)) == BLACK) {
                            setColor(rbNode, RED);
                            setColor(leftOf(rbNode), BLACK);
                            leftRotate(rbNode);
                            rbNode = leftOf(parentOf(x));
                        }
                        setColor(rbNode, colorOf(parentOf(x)));
                        setColor(parentOf(x), BLACK);
                        setColor(leftOf(rbNode), BLACK);
                        rightRotate(parentOf(x));
                        x = root;
                    }
                }
            }
            setColor(x, BLACK);
        }
    }


    public static RBNode successor(RBNode node) {
        if (node == null) {
            return null;
        }

        // 情况1：当前节点有右子树，后继是右子树的最小节点
        if (node.right != null) {
            return findMin(node.right);
        }

        // 情况2：当前节点无右子树，向上回溯找后继
        RBNode parent = node.parent;
        while (parent != null && parent.right == node) {
            node = parent;
            parent = node.parent;
        }
        return parent; // 若parent为null，说明当前节点是最大节点，无后继
    }

    // 辅助方法：查找子树中的最小节点（最左子节点）
    private static RBNode findMin(RBNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

}
