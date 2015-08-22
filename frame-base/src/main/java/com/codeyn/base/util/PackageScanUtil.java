package com.codeyn.base.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * 扫描指定的包及子包路径，返回满足条件的class数组
 * 包路径支持ant风格的模糊匹配
 * 如 com.sqq; com.sqq.*.config
 **/
public class PackageScanUtil {

    private static ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();

    public static interface ScannedClassFilter {
        boolean filter(Class<?> clazz);
    }

    public static Class<?>[] scanBasePackage(String... basePackages) {
        return scanBasePackage(null, basePackages);
    }

    public static Class<?>[] scanBasePackage(ScannedClassFilter filter, String... basePackages) {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        Set<Resource> resources = getResources(basePackages);
        for (Resource resource : resources) {
            Class<?> clazz = resolveClassFromResource(resource);
            if (filter == null || filter.filter(clazz)) {
                classSet.add(clazz);
            }
        }
        return classSet.toArray(new Class<?>[classSet.size()]);
    }

    private static Set<Resource> getResources(String[] basePackages) {
        Set<Resource> set = new LinkedHashSet<>();
        Resource[] resources;
        try {
            for (String basePackage : basePackages) {
                if (basePackage == null) {
                    basePackage = "";
                } else {
                    basePackage = basePackage.replace(".", "/");
                    if (!basePackage.endsWith("/")) {
                        basePackage += "/";
                    }
                }
                basePackage = "classpath*:" + basePackage + "**/*.class";
                resources = resourceLoader.getResources(basePackage);
                set.addAll(Arrays.asList(resources));
            }
        } catch (IOException e) {
            throw new RuntimeException("scan basePackage(s) error.", e);
        }
        return set;
    }

    private static Class<?> resolveClassFromResource(Resource resource) {
        ClassReader classReader;
        try (InputStream is = new BufferedInputStream(resource.getInputStream())) {
            classReader = new ClassReader(is);
        } catch (IOException ex) {
            throw new RuntimeException("ASM ClassReader failed to parse class file - "
                    + "probably due to a new Java class file version that isn't supported yet: " + resource, ex);
        }
        String className = classReader.getClassName().replace("/", ".");
        Class<?> clazz;
        try {
            clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            throw new RuntimeException("load class error.", e);
        }
        return clazz;
    }

}
