package prototype.v4;
//本代码演示了深克隆中的不完全深克隆问题
//深克隆:不仅复制所有基本数据类型字段，还会递归复制所有引用数据类型字段
public class Test {
    public static void main(String[] args) throws Exception {
        Person p1 = new Person();
        Person p2 = (Person) p1.clone();
        System.out.println("p1.loc == p2.loc?" + (p1.loc == p2.loc));
        p1.loc.street.reverse();
        System.out.println(p2.loc.street);
    }
}


class Person implements Cloneable {
    int age = 8;
    int score = 100;
    Location loc = new Location(new StringBuilder("bj"), 22);

    @Override
    protected Object clone() throws CloneNotSupportedException {

        Person p = (Person) super.clone();
        p.loc = (Location) loc.clone();
        return p;
    }
}

class Location implements Cloneable {
    StringBuilder street;//StringBuilder：为可变类型
    int roomNo;

    @Override
    public String toString() {
        return "Location{" +
                "street='" + street + '\'' +
                ", roomNo=" + roomNo +
                '}';
    }

    public Location(StringBuilder street, int roomNo) {
        this.roomNo = roomNo;
        this.street = street;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}