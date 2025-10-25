package strategy;

@FunctionalInterface
public interface Comparator<T> {
    int compare(T o1, T o2);
    //default：在接口中定义有实现的方法
    default void m() {
        System.out.println("m");
    }
}
