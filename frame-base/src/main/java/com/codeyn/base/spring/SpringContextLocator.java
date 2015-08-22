package com.codeyn.base.spring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The Class DefaultServiceLocator.
 */
public class SpringContextLocator {

    /**
     * 全局缓存locator对应的ApplicationContext
     */
    private static Map<String, ClassPathXmlApplicationContext> contexts = new ConcurrentHashMap<String, ClassPathXmlApplicationContext>();

    protected static String getSpringConfigFileName(Class<?> clz) {
        String currentLocatorName = clz.getSimpleName();
        StringBuffer configFileName = new StringBuffer("");
        for (int i = 0, j = currentLocatorName.length(); i < j; i++) {
            char tempChar = currentLocatorName.charAt(i);
            if (Character.isLowerCase(tempChar)) {
                configFileName.append(tempChar);
            } else if (Character.isUpperCase(currentLocatorName.charAt(i))) {
                configFileName.append("_");
                configFileName.append(Character.toLowerCase(tempChar));
            }
        }
        configFileName.append(".xml");
        return configFileName.substring(1, configFileName.length()).toString();
    }

    public static ApplicationContext loadContext(Class<?> clz) {
        String xmlName = getSpringConfigFileName(clz);
        return loadContext(xmlName);
    }

    /**
     * 根据配置文件名称获取ApplicationContext
     * 
     * @param xmlName
     * @return
     */
    public static ApplicationContext loadContext(String xmlName) {
        ClassPathXmlApplicationContext context = contexts.get(xmlName);
        if (context == null) {
            if (StringUtils.isNotBlank(xmlName)) {
                List<Object> beanList = new ArrayList<Object>();
                context = new ClassPathXmlApplicationContext();
                context.setConfigLocation(xmlName);

                /**
                 * 添加BeanFactoryPostProcessor的目的是获得原始的bean对象，
                 * 直接getBean拿到的可能是代理后的对象
                 */
                context.addBeanFactoryPostProcessor(createProcessor(beanList));

                /**
                 * 在初始化之前先把context放到缓存中
                 * 防止在初始化过程中存在循环调用，反复初始化
                 * 造成程序卡死
                 */
                contexts.put(xmlName, context);
                context.refresh();

                for (Object bean : beanList) {
                    // 解决跨locator的依赖
                    resolveInjects(bean);
                }
            }
        }
        return context;
    }

    private static BeanFactoryPostProcessor createProcessor(
            final List<Object> beanList) {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(
                    ConfigurableListableBeanFactory beanFactory)
                    throws BeansException {
                beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
                    @Override
                    public Object postProcessBeforeInitialization(Object bean,
                            String beanName) throws BeansException {
                        beanList.add(bean);
                        return bean;
                    }

                    @Override
                    public Object postProcessAfterInitialization(Object bean,
                            String beanName) throws BeansException {
                        return bean;
                    }
                });
            }
        };
    }

    private static void resolveInjects(Object bean) throws BeansException {
        Class<?> clazz = bean.getClass();
        Field[] fileds = clazz.getDeclaredFields();
        for (Field field : fileds) {
            Load injectAnno = field.getAnnotation(Load.class);
            if (injectAnno == null) {
                continue;
            }
            Class<?> locator = injectAnno.locator();

            ApplicationContext context = loadContext(locator);
            if (context == null) {
                continue;
            }
            Object target = null;
            String name = injectAnno.name();
            if ("".equals(name.trim())) {
                target = context.getBean(field.getType());
            } else {
                target = context.getBean(name, field.getType());
            }
            if (target != null) {
                try {
                    boolean acc = field.isAccessible();
                    if (!acc) {
                        field.setAccessible(true);
                    }
                    field.set(bean, target);
                    if (!acc) {
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("resolve inject error", e);
                }
            }
        }
    }
}
