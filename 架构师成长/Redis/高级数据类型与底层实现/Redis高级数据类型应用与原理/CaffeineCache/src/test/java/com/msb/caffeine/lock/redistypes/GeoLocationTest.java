package com.msb.caffeine.lock.redistypes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.params.GeoRadiusParam;

import java.util.List;

@SpringBootTest
public class GeoLocationTest {
    private Jedis jedis;
    public GeoLocationTest() {
        jedis =  new Jedis("127.0.0.1",6379);
    }

    @Test
    void testFindNearbyRiders() {
        String geoKey = "rider_locations";
        // 删除已有的geo数据
        jedis.del(geoKey);

        // 添加配送员位置（北京坐标）
        // 配送员在3公里范围内
        jedis.geoadd(geoKey, 116.4074, 39.9042, "rider_001"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4080, 39.9035, "rider_002"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4060, 39.9050, "rider_003"); // 靠近天安门
        jedis.geoadd(geoKey, 116.3960, 39.9090, "rider_004"); // 靠近天安门
        jedis.geoadd(geoKey, 116.3980, 39.9070, "rider_005"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4000, 39.9060, "rider_006"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4010, 39.9075, "rider_007"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4020, 39.9085, "rider_008"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4030, 39.9095, "rider_009"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4040, 39.9105, "rider_010"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4050, 39.9115, "rider_011"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4060, 39.9125, "rider_012"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4070, 39.9135, "rider_013"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4080, 39.9145, "rider_014"); // 靠近天安门
        jedis.geoadd(geoKey, 116.4090, 39.9155, "rider_015"); // 靠近天安门

        // 配送员不在3公里范围内
        jedis.geoadd(geoKey, 116.3000, 39.9000, "rider_016"); // 远离天安门
        jedis.geoadd(geoKey, 116.2000, 39.8000, "rider_017"); // 远离天安门
        jedis.geoadd(geoKey, 116.1000, 39.7000, "rider_018"); // 远离天安门
        jedis.geoadd(geoKey, 116.0000, 39.6000, "rider_019"); // 远离天安门
        jedis.geoadd(geoKey, 115.9000, 39.5000, "rider_020"); // 远离天安门


        // 用户位置（天安门）
        double userLon = 116.3974;
        double userLat = 39.9087;

        // 查询3公里内最近的10个配送员
        List<GeoRadiusResponse> riders = jedis.georadius(
                geoKey,
                userLon,
                userLat,
                3,
                GeoUnit.KM,
                GeoRadiusParam.geoRadiusParam()
                        .withDist()
                        .withCoord()
                        .count(10)
                        .sortAscending()
        );

        System.out.println("Nearby riders: ");
        for (GeoRadiusResponse rider : riders) {
            System.out.println(rider.getMemberByString() + " - Distance: " + rider.getDistance() + "km");
        }

        // 获取距离信息：正确计算两点间距离
        Double distance1to2 = jedis.geodist(geoKey, "rider_001", "rider_002", GeoUnit.KM);
        Double distance1to3 = jedis.geodist(geoKey, "rider_001", "rider_003", GeoUnit.KM);

        System.out.println("rider_001 to rider_002: " + distance1to2 + "km");
        System.out.println("rider_001 to rider_003: " + distance1to3 + "km");
    }
}