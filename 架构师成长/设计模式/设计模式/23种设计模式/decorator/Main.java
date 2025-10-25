package decorator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Main {

    public static void main(String[] args) throws Exception {
        File f = new File("c:/work/test.data");
        //创建文件输出字节流
        FileOutputStream fos = new FileOutputStream(f);
        //将字节流转化为字符流
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        //包装字符流，增加缓冲功能提升效率
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write("http://www.mashibing.com");
        bw.flush();
        bw.close();
    }
}
