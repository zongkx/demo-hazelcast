package com.zongkx;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationUtil {
    /**
     * 类缓存
     */
    private static final Map<Class<?>, Set<Class<?>>> map = new ConcurrentHashMap<>();

    /**
     * 扫描某个包下的所有包含某个注解的类
     */
    public static Set<Class<?>> findAnnotatedClasses(String basePackage, Class<? extends Annotation> annotationClass) {
        if (!map.containsKey(annotationClass)) {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

            Set<Class<?>> annotatedClasses = new HashSet<>();
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                try {
                    Class<?> annotatedClass = Class.forName(beanDefinition.getBeanClassName());
                    annotatedClasses.add(annotatedClass);
                } catch (ClassNotFoundException e) {
                    // 处理异常
                }
            }
            map.put(annotationClass, annotatedClasses);
            return annotatedClasses;
        } else {
            return map.get(annotationClass);
        }
    }
}
