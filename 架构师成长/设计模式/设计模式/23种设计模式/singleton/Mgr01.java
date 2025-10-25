package singleton;

/**
 * 饿汉式
 * 类加载到内存后，就实例化一个单例，JVM保证线程安全
 * 简单实用，推荐使用！
 * 唯一缺点：不管用到与否，类装载时就完成实例化
 * Class.forName("")
 * （话说你不用的，你装载它干啥）
 */
public class Mgr01 {
    //static:保证全局唯一一份
    private static final Mgr01 INSTANCE = new Mgr01();
    //私有方法构造，防止外部通过new方法创建实例
    private Mgr01() {
    }
    //公共方法，提供全局访问点
    public static Mgr01 getInstance() {
        return INSTANCE;
    }
    //单例的业务方法
    public void m() {
        System.out.println("m");
    }

    public static void main(String[] args) {
        Mgr01 m1 = Mgr01.getInstance();
        Mgr01 m2 = Mgr01.getInstance();
        System.out.println(m1 == m2);
    }
}
