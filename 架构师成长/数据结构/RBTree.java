package com.bobo.util.treemap;


public class RBTree<K extends Comparable<K> , V> {

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    // 红黑树的 root 节点
    private RBNode root;

    public RBNode getRoot() {
        return root;
    }

    public void setRoot(RBNode root) {
        this.root = root;
    }

    /**
     * 实现左旋
     *     p             pr
     *    /\            /\
     *   pl pr  ==>    p rr
     *      /\        /\
     *     rl rr    pl rl
     *  左旋操作：
     *     p-pl 和 pr-rr 的关系不需要调整
     *     需要调整的情况
     *     1. pr-rl 调整为 p-rl
     *        将rl变为p的右子节点
     *        将p设置为rl的父节点
     *     2.判断p是否有父节点
     *       有
     *          pr.parent = p.parent
     *          pr为 p.parent的子节点，到底是 左子节点还是右子节点呢？
     *          if p.parent.left == p
     *              p.parent.left = r
     *           else
     *              p.parent.right = r
     *       没有
     *         直接把pr设置为 root节点
     *      3.最后 p和pr 交换
     *        p.parent = r
     *        r.left = p
     *
     * @param p
     */
    private void leftRotate(RBNode p){
        if(p != null){
            // 获取到 pr 节点
            RBNode r = p.right;
            // 情况1：pr-rl 调整为 p-rl
            p.right = r.left;
            if(r.left != null){
                r.left.parent = p;
            }
            // 情况2： 判断p节点是否有父节点
            r.parent = p.parent; // 不管p是否存在父节点，我们都设置p的父节点也为 pr的父节点
            if(p.parent == null){
                // 说明p就是root节点，这时 pr 会变为新的root节点
                root = r;
            }// 说明p节点是存在父节点的
            else if(p.parent.left == p){
                // 说明p是父节点的左子节点，那么 pr 也肯定是p的父节点的左子节点
                p.parent.left = r;
            }else {
                p.parent.right = r;
            }
            // 情况3： 设置p为pr的左子节点
            r.left = p;
            p.parent = r;
        }
    }


    /**
     * 实现右旋
     *     p             pr
     *    /\            /\
     *   pl pr  <==    p rr
     *      /\        /\
     *     rl rr    pl rl
     *  左旋操作：
     *     p-pl 和 pr-rr 的关系不需要调整
     *     需要调整的情况
     *     1. pr-rl 调整为 p-rl
     *        将rl变为p的右子节点
     *        将p设置为rl的父节点
     *     2.判断p是否有父节点
     *       有
     *          pr.parent = p.parent
     *          pr为 p.parent的子节点，到底是 左子节点还是右子节点呢？
     *          if p.parent.left == p
     *              p.parent.left = r
     *           else
     *              p.parent.right = r
     *       没有
     *         直接把pr设置为 root节点
     *      3.最后 p和pr 交换
     *        p.parent = r
     *        r.left = p
     *
     * @param p
     */
    private void rightRotate(RBNode p){
        if(p != null){
            // 获取到 pr 节点
            RBNode r = p.left;
            // 情况1：pr-rl 调整为 p-rl
            p.left = r.right;
            if(r.right != null){
                r.right.parent = p;
            }
            // 情况2： 判断p节点是否有父节点
            r.parent = p.parent; // 不管p是否存在父节点，我们都设置p的父节点也为 pr的父节点
            if(p.parent == null){
                // 说明p就是root节点，这时 pr 会变为新的root节点
                root = r;
            }// 说明p节点是存在父节点的
            else if(p.parent.left == p){
                // 说明p是父节点的左子节点，那么 pr 也肯定是p的父节点的左子节点
                p.parent.left = r;
            }else {
                p.parent.right = r;
            }
            // 情况3： 设置p为pr的左子节点
            r.right = p;
            p.parent = r;
        }
    }

    /**
     * 红黑树 新增节点的操作
     * @param key
     * @param value
     */
    public void put(K key,V value){
        RBNode t = root;
        if(t == null){
            // 这是插入的第一个节点
            root = new RBNode(key,value == null ? key:value,null);
            return ;
        }
        int cmp;
        // 1. 找到插入的位置(找到新增节点的父节点)
        RBNode parent;
        do{
            parent = t;
            cmp = key.compareTo((K) t.key);
            if(cmp < 0){
                // 从左侧查找
                t = t.left;
            }else if(cmp > 0){
                // 从右侧查找
                t = t.right;
            }else{
                // 说明节点存在，用插入节点的值覆盖掉 相同的节点
                t.setValue(value==null?key:value);
                return ;
            }
        }while (t != null);
        // 2.将新节点添加到父节点的子节点中(左右子节点)
        // 创建要插入的节点
        RBNode node = new RBNode(key,value == null? key : value,parent);
        if(cmp < 0){
            // 将新节点添加到父节点的左侧
            parent.left = node;
        }else{
            // 添加到右侧
            parent.right = node;
        }
        // 旋转和变色 调整红黑树的平衡
        fixAfterPut(node);
    }

    private RBNode parentOf(RBNode node){
        return node != null ? node.parent :null;
    }

    private  RBNode leftOf(RBNode node){
        return node != null ? node.left:null;
    }

    private  RBNode rightOf(RBNode node){
        return node != null ? node.right :null;
    }

    private boolean colorOf(RBNode node){
        return node == null ? BLACK:node.color;
    }

    private void setColor(RBNode node,boolean color){
        if(node != null){
            node.setColor(color);
        }
    }


    /**
     * 插入节点后的调整操作
     * 2-3-4对应的操作
     *   2节点： 新插入一个元素 直接和2节点合并 不用调整
     *       红黑树：新增一个红色节点在黑色节点下 不需要调整
     *   3节点： 新插入一个元素在3节点下 那么会出现6中情况(2两种不需要调整，4中需要调整)
     *       红黑树： 插入的节点是 插入在 上黑下红的结构中，插入在红色节点
     *   4节点： 新插入一个元素在4节点下，那么会出现4中情况 都需要调整
     *       红黑树：新增的节点是红色 爷爷节点是黑色，父亲节点和叔叔节点是红色
     * @param x
     */
    private void fixAfterPut(RBNode<K,Object> x){
        // 插入的节点 肯定红色
        x.color = RED;
        // 2节点不用调整，3,4节点才需要调整
        while( x != null && x != root && x.parent.color == RED){
           if(parentOf(x) == leftOf(parentOf(parentOf(x)))){
                // 需要调整的只剩下4种情况，有叔叔节点和没有叔叔节点
                // 获取插入节点的叔叔节点
               RBNode y = rightOf(parentOf(parentOf(x)));
               if(colorOf(y) == RED){
                   // 说明是有叔叔节点的
                   // 变色+递归
                   // 父亲和叔叔变为黑色  爷爷变为红色
                   setColor(parentOf(x),BLACK);
                   setColor(y,BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   // 递归处理
                   x = parentOf(parentOf(x));
               }else{
                    // 说明是没有叔叔节点的
                   if(x == parentOf(x).right){
                       x = parentOf(x);
                       leftOf(x);
                   }
                   // 父亲节点变为黑色 爷爷节点变为红色
                   setColor(parentOf(x),BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   // 根据爷爷节点做右旋操作
                   rightRotate(parentOf(parentOf(x)));
               }

           }else{
                // 需要调整的也是4中情况，刚好和上面的4中情况左右相反
               // 需要调整的只剩下4种情况，有叔叔节点和没有叔叔节点
               // 获取插入节点的叔叔节点
               RBNode y = leftOf(parentOf(parentOf(x)));
               if(colorOf(y) == RED){
                   // 说明是有叔叔节点的
                   // 变色+递归
                   // 父亲和叔叔变为黑色  爷爷变为红色
                   setColor(parentOf(x),BLACK);
                   setColor(y,BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   // 递归处理
                   x = parentOf(parentOf(x));
               }else{
                   // 说明是没有叔叔节点的
                   if(x == parentOf(x).left){
                       x = parentOf(x);
                       rightOf(x);
                   }
                   // 父亲节点变为黑色 爷爷节点变为红色
                   setColor(parentOf(x),BLACK);
                   setColor(parentOf(parentOf(x)),RED);
                   // 根据爷爷节点做右旋操作
                   leftRotate(parentOf(parentOf(x)));
               }
           }
        }
        // root 节点肯定为黑色
        root.color = BLACK;
    }

    /**
     * 删除节点
     *   节点删除(普通的二叉树删除)
     *   删除后的调整
     * @return
     */
    public V remove(K key){
        // 1. 根据需要删除的key 找到对应的Node节点
        RBNode node = getNode(key);
        if(node == null){
            // 不存在
            return null;
        }
        V oldValue = (V) node.value;
        // 具体删除节点的方法
        deletNode(node);
        return oldValue;
    }

    /**
     * 删除节点
     * 1.删除节点(普通的二叉树删除)
     *    1.删除叶子节点 直接删除
     *    2.删除节点有一个子节点，那么用子节点来替代
     *    3.删除节点右两个子节点，这时我们需要找到删除节点的前驱节点或者后继节点来替换
     *          将情况3转换为情况1或2
     * 2.调整
     * @param node
     */
    private void deletNode(RBNode node) {
        // 1. 先处理情况3
        if(leftOf(node) != null && rightOf(node) != null){
            // 说明要删除的节点有两个子节点 就要找到需要删除节点的 后继节点/前驱节点
            // RBNode pNode = successor(node);
            RBNode pNode = predecessor(node);
            // 用后继节点的值 覆盖原来要删除的节点
            node.key = pNode.key;
            node.value = pNode.value;
            // 这时要删除的节点就变为了 后继几点
            node = pNode;
        }
        // 获取对应的替换节点
        RBNode replacement = node.left != null ? node.left:node.right;
        if(replacement != null){
            // 2.再处理情况2  情况2也可能是情况3转换过来的
            replacement.parent = node.parent;
            if(node.parent == null){
                root = replacement;
            }else if(leftOf(parentOf(node)) == node){
                parentOf(node).left = replacement;
            }else{
                parentOf(node).right = replacement;
            }
            // 要删除的node节点 GC
            node.left = node.right = node.parent = null;
            if(colorOf(node) == BLACK){
                // 做调整操作
                fixAfterRemove(replacement);
            }
        }else if(node.parent == null){
            // 说明我们要删除的就是root节点
            root = null;
        }
        else{
            // 3.处理情况1
            // 先调整 再删除
            if(node.color == BLACK){
                fixAfterRemove(node);
            }
            if(node.parent != null){
                if(leftOf(parentOf(node)) == node){
                    parentOf(node).left = null;
                }else{
                    parentOf(node).right = null;
                }
                node = null;
            }
        }
    }

    /**
     * 删除节点后的调整操作
     * 2-3-4树删除操作
     * 1.删除3、4节点 自己能搞定的
     * 2.删除2节点 自己搞不定，需要兄弟节点借， 兄弟节点借
     *      父亲节点下来，兄弟节点找一个节点替换掉父亲节点的位置
     * 3.删除2节点 自己搞不定，需要兄弟借  兄弟不借
     * @param e
     */
    private void fixAfterRemove(RBNode e) {
        // 情况2和3
        while(e != root && colorOf(e) == BLACK){
            // 判断 e 是父节点的左孩子还是右孩子
            if(e == leftOf(parentOf(e))){
                // 1.找到兄弟节点
                RBNode rNode = parentOf(e).right;
                // 判断找到的是不是真的兄弟节点
                if(colorOf(rNode) == RED){
                    // 找到的就不是真正的兄弟节点 需要一次变色加旋转操作
                    setColor(rNode,BLACK);
                    setColor(parentOf(e),RED);
                    leftRotate(parentOf(e)); // 完成一次左旋操作
                    rNode = parentOf(e).right;
                }
                // 当兄弟节点一个子节点都没有的情况下 不借
                if(colorOf(leftOf(rNode)) == BLACK && colorOf(rightOf(rNode)) == BLACK){
                    // 3.兄弟节点 不借
                    setColor(rNode,RED);
                    e = parentOf(e);
                }else{
                    // 2.兄弟节点 借
                    // 如果兄弟节点的子节点是其左子节点 需要先变色 完成右转一次
                    if(colorOf(rightOf(rNode)) == BLACK){
                        // 右侧子节点为空，那么左侧子节点肯定不为空
                        setColor(rNode,RED);
                        setColor(leftOf(rNode),BLACK);
                        rightRotate(rNode);
                        rNode = rightOf(parentOf(e));
                    }
                    // 需要根据父节点做一次左旋操作 变色
                    setColor(rNode,colorOf(parentOf(e)));
                    setColor(parentOf(e),BLACK);
                    setColor(rightOf(rNode),BLACK);
                    // 左旋操作
                    leftRotate(parentOf(e));
                    e = root; // 介绍循环  针对情况3的处理
                }
            }else{
                // 1.找到兄弟节点
                RBNode rNode = parentOf(e).left;
                // 判断找到的是不是真的兄弟节点
                if(colorOf(rNode) == RED){
                    // 找到的就不是真正的兄弟节点 需要一次变色加旋转操作
                    setColor(rNode,BLACK);
                    setColor(parentOf(e),RED);
                    rightRotate(parentOf(e)); // 完成一次左旋操作
                    rNode = parentOf(e).left;
                }
                // 当兄弟节点一个子节点都没有的情况下 不借
                if(colorOf(leftOf(rNode)) == BLACK && colorOf(rightOf(rNode)) == BLACK){
                    // 3.兄弟节点 不借
                    setColor(rNode,RED);
                    e = parentOf(e);
                }else{
                    // 2.兄弟节点 借
                    // 如果兄弟节点的子节点是其左子节点 需要先变色 完成右转一次
                    if(colorOf(leftOf(rNode)) == BLACK){
                        // 右侧子节点为空，那么左侧子节点肯定不为空
                        setColor(rNode,RED);
                        setColor(rightOf(rNode),BLACK);
                        leftRotate(rNode);
                        rNode = leftOf(parentOf(e));
                    }
                    // 需要根据父节点做一次左旋操作 变色
                    setColor(rNode,colorOf(parentOf(e)));
                    setColor(parentOf(e),BLACK);
                    setColor(leftOf(rNode),BLACK);
                    // 左旋操作
                    rightRotate(parentOf(e));
                    e = root; // 介绍循环  针对情况3的处理
                }
            }
        }

        // 情况1： 替换的节点为红色，我们只需要变色为黑色即可
        setColor(e,BLACK);
    }

    /**
     * 根据key找到对应的node
     * @param key
     * @return
     */
    private RBNode getNode(K key) {
        RBNode node = this.root;
        while(node != null){
            int cmp = key.compareTo((K) node.key);
            if(cmp < 0){
                node = node.left;
            }else if(cmp > 0){
                node = node.right;
            }else{
                // 表示找到了对应的节点
                return node;
            }
        }
        return null;
    }

    /**
     * 获取前驱节点
     *   找到指定节点的前驱节点，即找到小于node节点的最大值
     * @param node
     */
    private RBNode predecessor(RBNode node){
        if(node == null){
            return  null;
        }else if(node.left != null){
            // 如果当前节点有左子节点，然后就根据左子节点找到它的最右子节点既是前驱节点
            RBNode p = node.left;
            while(p.right != null){
                p = p.right;
            }
            return p;
        }else{
            // 这种情况在删除中是不会出现的，但是对于查找前驱节点来说我们还是要来实现的一种情况
            // 就是如果当前节点没有左子节点，那么我们就需要向上找
            RBNode p = node.parent;
            RBNode ch = node;
            while(p != null && ch == p.left){
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * 后继节点
     * @param node
     * @return
     */
    private RBNode successor(RBNode node){
        if(node==null){
            return  null;
        }else if(node.right != null){
            RBNode p = node.right;
            while (p.left != null){
                p = p.left;
            }
            return p;
        }else{
            RBNode p = node.parent;
            RBNode ch = node;
            while(p!= null && ch == p.right){
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    static class RBNode<K extends Comparable<K> ,V>{

        private RBNode parent;

        private RBNode left;

        private RBNode right;

        private boolean color;

        private K key;

        private V value;


        public RBNode() {
        }

        public RBNode(RBNode parent, RBNode left, RBNode right, boolean color, K key, V value) {
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.color = color;
            this.key = key;
            this.value = value;
        }

        public RBNode( K key, V value,RBNode parent) {
            this.parent = parent;
            this.key = key;
            this.value = value;
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
    }


}
