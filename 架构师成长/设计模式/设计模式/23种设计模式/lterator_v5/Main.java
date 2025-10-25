package lterator_v5;


//用一种统一的遍历方式，要求每一个容器都要提供Iterator的实现类
public class Main {

    public static void main(String[] args) {
        Collection_ list = new Arraylist_();
        for (int i = 0; i < 15; i++) {
            list.add(new String("s" + i));
        }
        System.out.println(list.size());

        Iterator_ it = list.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            System.out.println(o);
        }
    }
}
