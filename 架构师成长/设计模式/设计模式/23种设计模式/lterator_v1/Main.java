package lterator_v1;

//构建一个容器，可以添加对象

import state_thread.NewState;

public class Main {

    public static void main(String[] args) {
        ArrayList_ list = new ArrayList_();
        for (int i = 0; i < 15; i++) {
            list.add(new String("s" + i));
        }
        System.out.println(list.size());
    }
}

//相比数组，这个容器不用考虑边界问题，可以动态拓展
class ArrayList_ {
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
}