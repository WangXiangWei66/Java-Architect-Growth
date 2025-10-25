package lterator_v4;

//如何对容器实现遍历
public class Main {

    public static void main(String[] args) {
        Collection_ list = new Arraylist_();
        for (int i = 0; i < 15; i++) {
            list.add(new String("s" + i));
        }
        System.out.println(list.size());

        Arraylist_ al = (Arraylist_) list;
        for(int i = 0;i < al.size();i++) {
            //这种遍历方式，无法实现通用
        }
    }
}
