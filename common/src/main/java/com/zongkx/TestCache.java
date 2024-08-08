package com.zongkx;

@MyCache
public class TestCache {
    @MyCache(liveSeconds = 60, desc = "测试缓存")
    public static final String TEST = "CACHE_TEST";
}
