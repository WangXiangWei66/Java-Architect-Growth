package ASM;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static org.objectweb.asm.Opcodes.ASM4;

public class ClassPrinter extends ClassVisitor {
    public ClassPrinter() {
        super(ASM4);//指定ASM版本
    }
    /*
    * version：类的字节码版本
    * access：类的访问权限
    * name：类的全限定名
    * signature：泛型签名
    * superName:父类的全限定名
    * interfaces：实现的接口数组
    * */
    @Override//解析类的基础信息
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println(name + " extends " + superName + "{");
    }
    /*
    access：字段访问权限（如 ACC_PRIVATE 对应 private）；
    name：字段名（如 age、name）；
    descriptor：字段的类型描述符（如 I 表示 int，Ljava/lang/String; 表示 String）；
    signature：泛型字段的签名（无泛型时为 null）；
    value：字段的默认值（如 static final int a = 10 中的 10）。
     */
    @Override//解析类的字段
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        System.out.println("       " + name);
        return null;
    }
    /*
    access：方法访问权限（如 ACC_PUBLIC、ACC_STATIC）；
    name：方法名（构造方法为 <init>，静态代码块为 <clinit>）；
    descriptor：方法的方法描述符（如 (ILjava/lang/String;)V 表示参数为 int 和 String、返回值为 void）；
    signature：泛型方法的签名；
    exceptions：方法抛出的异常数组。
     */
    @Override//解析类的方法
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        System.out.println("    " + name + "()");
        return null;
    }

    @Override
    public void visitEnd() {
        System.out.println("}");
    }



    public static void main(String[] args) throws IOException {
        ClassPrinter cp = new ClassPrinter();
        ClassReader cr = new ClassReader(ClassPrinter.class.getClassLoader().getResourceAsStream("ASM/T1.class"));

        cr.accept(cp, 0);
    }
}
