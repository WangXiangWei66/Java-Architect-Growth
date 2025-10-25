package src.main.java.SystemIO.netty;
//RPC完整消息（头+数据）
public class Packmsg {

    MyHeader header;//消息头部
    MyContent content;//消息数据

    public Packmsg(MyHeader header, MyContent content) {
        this.header = header;
        this.content = content;
    }

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    public MyContent getContent() {
        return content;
    }

    public void setContent(MyContent content) {
        this.content = content;
    }
}
