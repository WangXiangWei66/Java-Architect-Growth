package com.msb.caffeine.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.UUID;

public class SimpleLockWithWatchdog {

    private static final String LOCK_KEY = "simple_lock";
    private static final int LOCK_EXPIRE_TIME = 30000; // 锁的过期时间，单位毫秒
    private static final int WATCHDOG_INTERVAL = 15000; // 看门狗线程轮询间隔，单位毫秒

    private static boolean isLocked = false;

    private  static String lockvalue="";

    private static String  Release_lock_Lua=" if redis.call('get',KEYS[1]) == ARGV[1] then" +
                                            "       return redis.call('del',KEYS[1)" +
                                            "  else return 0 end   ";

    public static void main(String[] args) {
        Jedis jedis =  new Jedis("127.0.0.1",6379);


        // new thread  -> 拿锁
        // new thread  --> 先释放锁，再 拿锁
        // 释放分布式锁
        releaseLock(jedis);
        acquireLock(jedis);
        releaseLock(jedis);

        // 获取分布式锁
        if (acquireLock(jedis)) {
            System.out.println("获得分布式锁成功");

            // 模拟业务处理
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 释放分布式锁
            releaseLock(jedis);
            System.out.println("释放分布式锁成功");
        } else {
            System.out.println("获取分布式锁失败");
        }

        jedis.close();
    }

    private static boolean acquireLock(Jedis jedis) {
        long startTime = System.currentTimeMillis();
        while (true) {
            SetParams params = new SetParams();
            params.px(LOCK_EXPIRE_TIME);
            params.nx();
            //这里 加标识 唯一性的标识
            String id = UUID.randomUUID().toString();
            String result = jedis.set(LOCK_KEY, id, params);
            if ("OK".equals(result)) {
                lockvalue=id;
                isLocked = true;
                startWatchdogThread(jedis);
                return true;
            }

            // 判断是否超时
            if (System.currentTimeMillis() - startTime > LOCK_EXPIRE_TIME) {
                return false;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void releaseLock(Jedis jedis) {
        if (isLocked) { //只有持有锁的线程才能释放锁---需要通过 本地存储的唯一性的ID 和redis中写入的ID（加锁成功后）
            //这里就不能通过单纯的是否加锁的标识来去解锁(担心 应用 乱写：先释放锁，再 加锁)
            Long result= (Long)jedis.eval(Release_lock_Lua, Arrays.asList(LOCK_KEY),Arrays.asList(lockvalue));
            if(result.longValue()!=0){
                //这里是释放锁成功
            }else{
                //这里是释放锁失败
            }
//            jedis.del(LOCK_KEY);
//            isLocked = false;
        }
    }

    private static void startWatchdogThread(Jedis jedis) {
        Thread watchdogThread = new Thread(() -> {
            while (isLocked) {
                // 每隔一段时间对锁进行续期
                jedis.pexpire(LOCK_KEY, LOCK_EXPIRE_TIME);
                try {
                    Thread.sleep(WATCHDOG_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        watchdogThread.setDaemon(true);//守护线程（如果拿锁线程 挂了，守护也会跟着挂）
        watchdogThread.start();
    }
}