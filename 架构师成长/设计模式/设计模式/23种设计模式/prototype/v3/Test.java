package prototype.v3;

//深克隆:不仅复制所有基本数据类型字段，还会递归复制所有引用数据类型字段
public class Test {
    public static void main(String[] args) throws Exception {
        Person p1 = new Person();
        Person p2 = (Person) p1.clone();
        System.out.println(p2.age + " " + p2.score);
        System.out.println(p2.loc);

        System.out.println(p1.loc == p2.loc);//打印false，说明不在指向同一个字段了
        p1.loc.street = "sh";
        System.out.println(p2.loc);
        //String作为不可变类，replace方法不会修改她
        p1.loc.street.replace("sh", "sz");
        System.out.println(p2.loc.street);
    }
}


class Person implements Cloneable {
    int age = 8;
    int score = 100;
    Location loc = new Location("bj", 22);

    @Override
    protected Object clone() throws CloneNotSupportedException {

        Person p = (Person) super.clone();
        p.loc = (Location) loc.clone();
        return p;
    }
}

class Location implements Cloneable {
    String street;
    int roomNo;

    @Override
    public String toString() {
        return "Location{" +
                "street='" + street + '\'' +
                ", roomNo=" + roomNo +
                '}';
    }

    public Location(String street, int roomNo) {
        this.roomNo = roomNo;
        this.street = street;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}