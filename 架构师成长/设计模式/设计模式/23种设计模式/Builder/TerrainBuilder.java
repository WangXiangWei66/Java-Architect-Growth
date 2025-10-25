package Builder;

public interface TerrainBuilder {
    //支持链式调用
    TerrainBuilder buildWall();
    TerrainBuilder buildFort();
    TerrainBuilder buildMine();
    Terrain build();
}
