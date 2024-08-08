package com.zongkx;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;
import com.hazelcast.map.IMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Lazy(value = false)
public class CacheUtil {
    @Getter
    private static HazelcastInstance hazelcastInstance;

    public CacheUtil(@Qualifier("myHazelcastInstance") HazelcastInstance hazelcastInstance) {
        CacheUtil.hazelcastInstance = hazelcastInstance;
    }

    /**
     * 获取分布式信号量
     *
     * @param permits 许可数量
     * @return semaphore
     */
    public static ISemaphore semaphore(int permits, String name) {
        ISemaphore leader = hazelcastInstance.getCPSubsystem().getSemaphore(name);
        leader.init(permits);
        return leader;
    }

    public static void evict(String name, String key) {
        IMap<Object, Object> map = hazelcastInstance.getMap(name);
        Object o = map.get(key);
        boolean evict = map.evict(key);
        log.info("丢弃缓存{}成功/失败:{}-----------------------{}:{}", name, evict, key, o);
    }

    public static Object get(String name, String key) {
        IMap<Object, Object> map = hazelcastInstance.getMap(name);
        Object o = map.get(key);
        log.info("获取缓存{}-----------------------{}:{}", name, key, o);
        return o;
    }

    public static String getString(String name, String key) {
        Object o = get(name, key);
        return Objects.isNull(o) ? null : String.valueOf(o);
    }

    public static Integer getInt(String name, String key) {
        Object o = get(name, key);
        return Objects.isNull(o) ? null : Integer.parseInt(o.toString());
    }

    public static LocalDateTime getTime(String name, String key) {
        Object o = get(name, key);
        return Objects.isNull(o) ? null : (o instanceof LocalDateTime ? (LocalDateTime) o : null);
    }

    public static <T> T getObj(String name, String key) {
        Object o = get(name, key);
        try {
            return Objects.isNull(o) ? null : (T) o;
        } catch (Exception e) {
            throw e;
        }
    }

    public static void set(String name, String key, Object value) {
        IMap<Object, Object> map = hazelcastInstance.getMap(name);
        map.put(key, value);
        log.info("设置缓存{}-----------------------{}:{}", name, key, value);
    }

    public static void setTtl(String name, String key, Object value, long ttl, TimeUnit ttlUnit) {
        IMap<Object, Object> map = hazelcastInstance.getMap(name);
        map.put(key, value, ttl, ttlUnit);
        log.info("设置缓存{}-----------------------{}:{},时间{},单位{}", name, key, value, ttl, ttlUnit);
    }

    @PostConstruct
    public void init() {
        log.info("cache util init success!");
    }


}
