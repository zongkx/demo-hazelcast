package com.zongkx;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.topic.ITopic;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(prefix = "comen.cache.hazelcast", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CacheConfig {


    @Value("${spring.cache.hazelcast.cluster:false}")
    private Boolean cluster;
    @Value("${spring.cache.hazelcast.dns:hazelcast.default.svc.cluster.local}")
    private String dns;
    @Value("${spring.application.name:default}")
    private String appName;

    @Bean
    @ConditionalOnMissingBean//缓存管理器的自定义器
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    HazelcastCacheManager cacheManager(CacheManagerCustomizers customizers) {
        HazelcastCacheManager cacheManager = new HazelcastCacheManager(hazelcastInstance(config()));
        return customizers.customize(cacheManager);
    }


    @Bean
    @SneakyThrows
    public Config config() {
        Config config = new Config();
        config.setClusterName(appName + "-cache");
        if (Objects.equals(cluster, true)) {
            NetworkConfig networkConfig = new NetworkConfig();
            networkConfig.setJoin(new JoinConfig().setMulticastConfig(new MulticastConfig().setEnabled(false)));
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("service-dns", dns);
        } else {
            config.setNetworkConfig(new NetworkConfig().setJoin(new JoinConfig().setAutoDetectionConfig(new AutoDetectionConfig().setEnabled(true))));
        }
        Set<Class<?>> annotatedClasses = AnnotationUtil.findAnnotatedClasses("com.zongkx", MyCache.class);
        for (Class<?> annotatedClass : annotatedClasses) {
            Field[] declaredFields = annotatedClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                MyCache annotation = declaredField.getAnnotation(MyCache.class);
                String value = (String) declaredField.get(null);
                config.addMapConfig(new MapConfig().setName(value).setTimeToLiveSeconds(annotation.liveSeconds()));
            }
        }
        return config;
    }

    @Bean("myHazelcastInstance")
    public HazelcastInstance hazelcastInstance(Config config) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        //发布/订阅模式
        ITopic<String> topic = hazelcastInstance.getTopic("HAZELCAST_TOPIC");
        // 新增topic的监听者，具体实现加后文
        topic.addMessageListener(new TopicListener());
        return hazelcastInstance;
    }

}
