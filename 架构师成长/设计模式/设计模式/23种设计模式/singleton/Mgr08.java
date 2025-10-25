package singleton;

/**
 * 不仅可以解决线程同步，还可以防止反序列化。
 */
public enum Mgr08 {

    INSTANCE;//枚举的唯一实例，相当于单例的全局访问点

    public void m() {

    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println(Mgr08.INSTANCE.hashCode());
            }).start();
        }
    }
}
