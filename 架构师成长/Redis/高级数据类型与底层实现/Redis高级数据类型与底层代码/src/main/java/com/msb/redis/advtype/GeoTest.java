package com.msb.redis.advtype;

import redis.clients.jedis.*;
import redis.clients.jedis.params.GeoRadiusParam;

import java.util.List;

public class GeoTest {
    public static void main(String[] args) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 30000);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();


            jedis.geoadd("geo-cities",116.28,39.55,"beijing");
            jedis.geoadd("geo-cities",117.12,39.08,"tianjin");
            jedis.geoadd("geo-cities",114.29,38.02,"shijiazhuang");
            jedis.geoadd("geo-cities",118.01,39.38,"tangshan");
            jedis.geoadd("geo-cities",115.29,38.51,"baoding");

            GeoRadiusParam geoRadiusParam = new GeoRadiusParam();
            geoRadiusParam.withDist();
            //geoRadiusParam.sortAscending(); //升序
            geoRadiusParam.sortDescending();//降序
            List<GeoRadiusResponse> responses =jedis.georadiusByMember("geo-cities", "beijing", 150, GeoUnit.KM,geoRadiusParam);
            for(GeoRadiusResponse city:responses){
                System.out.println(city.getMemberByString());
                System.out.println(city.getDistance()+"KM");
                //System.out.println(city.getCoordinate());
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }
}
