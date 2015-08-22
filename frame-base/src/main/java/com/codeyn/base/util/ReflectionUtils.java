package com.codeyn.base.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.util.Assert;

/**
 * The Class ReflectionUtils.
 */
public class ReflectionUtils {

    /**
     * Invoke method.
     * 
     * @param owner
     *            the owner
     * @param methodName
     *            the method name
     * @param args
     *            the args
     * @return the object
     * @throws Exception
     *             the exception
     */
    public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
        Class<?> ownerClass = owner.getClass();
        Class<?>[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(owner, args);
    }

    /**
     * Invoke static method.
     * 
     * @param className
     *            the class name
     * @param methodName
     *            the method name
     * @param args
     *            the args
     * @return the object
     * @throws Exception
     *             the exception
     */
    public static Object invokeStaticMethod(String className, String methodName, Object[] args) throws Exception {
        Class<?> ownerClass = Class.forName(className);
        Class<?>[] argsClass = new Class[args.length];
        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }
        Method method = ownerClass.getMethod(methodName, argsClass);
        return method.invoke(null, args);
    }

    /**
     * 按FiledName获得Field的类型.
     */
    public static Class<?> getPropertyType(Class<?> type, String name) throws NoSuchFieldException {
        return getDeclaredField(type, name).getType();
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @param object
     * @param propertyName
     * @return
     * @throws NoSuchFieldException
     */
    public static Field getDeclaredField(Object object, String propertyName) throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);
        return getDeclaredField(object.getClass(), propertyName);
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Class<?> clazz, String propertyName) throws NoSuchFieldException {
        Assert.notNull(clazz);
        Assert.hasText(propertyName);
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(propertyName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + propertyName);
    }

    /**
     * 暴力获取对象变量值,忽略private,protected修饰符的限制.
     * 
     * @param object
     * @param propertyName
     * @return
     * @throws NoSuchFieldException
     */
    public static Object forceGetProperty(Object object, String propertyName) throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);
        Field field = getDeclaredField(object, propertyName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        } finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * 暴力设置对象变量值,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static void forceSetProperty(Object object, String propertyName, Object newValue)
            throws NoSuchFieldException {
        Assert.notNull(object);
        Assert.hasText(propertyName);
        Field field = getDeclaredField(object, propertyName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
        }
        field.setAccessible(accessible);
    }

    /**
     * 获取指定filed的泛型实际类型
     * @author leyuanren
     * @param field
     * @return
     */
    public static Type[] getFiledGenericTypes(Field field) {
        if (field == null) {
            return null;
        }
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        }
        return null;
    }

}
