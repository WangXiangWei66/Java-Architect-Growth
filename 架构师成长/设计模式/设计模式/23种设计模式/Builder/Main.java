package Builder;

public class Main {

    public static void main(String[] args) {
        TerrainBuilder builder = new ComplexTerrainBuilder();
        Terrain t = builder.buildFort().buildMine().buildWall().build();
        Person p = new Person.PersonBuilder().basicInfo(1, "zhangsan", 18).weight(200).build();
    }
}
