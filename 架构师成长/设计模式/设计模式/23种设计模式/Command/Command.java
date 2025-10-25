package Command;

public abstract class Command {
    //执行命令的抽象方法
    public abstract void doit();
    //撤销命令的抽象方法
    public abstract void undo();
}
