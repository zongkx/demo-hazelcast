# doc

不需要xml 配置的情况下 使用hazelcast 分布式缓存

通过自定义注解 @MyCache 标记cache的

```java

@MyCache
public class TestCache {
    @MyCache(liveSeconds = 60, desc = "测试缓存")
    public static final String TEST = "CACHE_TEST";
}


```