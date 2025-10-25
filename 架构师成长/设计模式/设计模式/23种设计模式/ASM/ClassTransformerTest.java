package ASM;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;//调用静态方法的字节码指令
import java.io.File;
import java.io.FileOutputStream;


public class ClassTransformerTest {

    public static void main(String[] args) throws Exception {
        ClassReader cr = new ClassReader(ClassPrinter.class.getClassLoader().getResourceAsStream("ASM/Tank.class"));
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ClassVisitor(ASM4, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(ASM4, mv) {
                    @Override
                    public void visitCode() {
                        visitMethodInsn(INVOKESTATIC, "ASM/TimeProxy", "before", "()V", false);
                        super.visitCode();
                    }
                };
            }
        };
        cr.accept(cv, 0);
        byte[] b2 = cw.toByteArray();

        MyClassLoader c1 = new MyClassLoader();
        c1.loadClass("ASM.TimeProxy");
        Class c2 = c1.defineClass("ASM.Tank", b2);
        c2.getConstructor().newInstance();//反射创建实例

        String path = (String) System.getProperties().get("user.dir");
        File f = new File(path + "/bridge/");
        f.mkdirs();
        FileOutputStream fos = new FileOutputStream(new File(path + "/ASM/Tank_0.class"));
        fos.write(b2);
        fos.flush();
        fos.close();
    }
}
