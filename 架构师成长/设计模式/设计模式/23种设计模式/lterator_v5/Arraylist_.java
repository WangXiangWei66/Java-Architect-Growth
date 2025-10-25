package lterator_v5;

class Arraylist_ implements Collection_ {

    //相比数组，这个容器不用考虑边界问题，可以动态拓展
    //底层存储数据的数组
    Object[] objects = new Object[10];

    private int index = 0;

    public void add(Object o) {
        if (index == objects.length) {
            Object[] newObjects = new Object[objects.length * 2];
            System.arraycopy(objects, 0, newObjects, 0, objects.length);
            objects = newObjects;
        }
        //将元素添加到数组的当前位置
        objects[index] = o;
        index++;
    }

    public int size() {
        return index;
    }

    @Override
    public Iterator_ iterator() {
        return new ArrayListIterator();//返回该容器的迭代器
    }
    //针对数组容器的具体迭代器
    private class ArrayListIterator implements Iterator_ {

        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            if (currentIndex >= index) return false;
            return true;
        }

        @Override
        public Object next() {
            Object o = objects[currentIndex];
            currentIndex++;
            return o;
        }
    }
}

