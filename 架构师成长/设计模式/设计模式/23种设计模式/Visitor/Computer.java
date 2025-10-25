package Visitor;

//不修改对象结构的前提下，为不同类型的对象动态添加新的操作
public class Computer {
    ComputerPart cpu = new CPU();//cpu
    ComputerPart memory = new Memory();//内存
    ComputerPart board = new Board();//主板
    //接收访问者，让访问者遍历所有元素
    public void accept(Visitor v) {
        this.cpu.accept(v);
        this.memory.accept(v);
        this.board.accept(v);
    }

    public static void main(String[] args) {
        //创建具体的访问者
        PersonelVisitor p = new PersonelVisitor();
        //创建对象结构来接收访问者
        new Computer().accept(p);
        System.out.println(p.totalPrice);
    }
}
//抽象类
abstract class ComputerPart {
    //接收访问者
    abstract void accept(Visitor v);
    //获取访问者的价格
    abstract double getPrice();
}

interface Visitor {
    void visitCpu(CPU cpu);

    void visitMemory(Memory memory);

    void visitBoard(Board board);
}

class CPU extends ComputerPart {

    @Override
    void accept(Visitor v) {
        v.visitCpu(this);//将自身传递给visitCPU方法
    }

    @Override
    double getPrice() {
        return 500;//返回CPU的基础价格
    }
}

class Memory extends ComputerPart {

    @Override
    void accept(Visitor v) {
        v.visitMemory(this);
    }

    @Override
    double getPrice() {
        return 300;
    }
}

class Board extends ComputerPart {

    @Override
    void accept(Visitor v) {
        v.visitBoard(this);
    }

    @Override
    double getPrice() {
        return 200;
    }
}
//实现抽象访问者接口，定义对不同元素的具体操作
class PersonelVisitor implements Visitor {
    double totalPrice = 0.0;

    @Override
    public void visitCpu(CPU cpu) {
        totalPrice += cpu.getPrice() * 0.9;
    }

    @Override
    public void visitMemory(Memory memory) {
        totalPrice += memory.getPrice() * 0.85;
    }

    @Override
    public void visitBoard(Board board) {
        totalPrice += board.getPrice() * 0.95;
    }
}

class CorpVisitor implements Visitor {
    double totalPrice = 0.0;

    @Override
    public void visitCpu(CPU cpu) {
        totalPrice += cpu.getPrice() * 0.6;
    }

    @Override
    public void visitMemory(Memory memory) {
        totalPrice += memory.getPrice() * 0.75;
    }

    @Override
    public void visitBoard(Board board) {
        totalPrice += board.getPrice() * 0.75;
    }
}