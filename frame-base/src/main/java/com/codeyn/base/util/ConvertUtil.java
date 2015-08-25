package com.codeyn.base.util;

import java.util.Date;

/**
 * 类ConvertUtil.java的实现描述：通用的对象转换工具类
 * 
 */
public class ConvertUtil {

    /**
     * 日期对象转时间戳，对象为null返回0
     * 
     * @param date
     * @return
     */
    public static long dateToTime(Date date) {
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }

    /**
     * 时间戳转日期对象，时间戳为0返回null
     * 
     * @param date
     * @return
     */
    public static Date timeToDate(long time) {
        if (time == 0) {
            return null;
        }
        return new Date(time);
    }

    /**
     * 获取枚举对象的名称
     * 
     * @param e
     * @return
     */
    public static String enumName(Enum<?> e) {
        if (e == null) {
            return null;
        }
        return e.name();
    }

    /**
     * 根据枚举对象名称和枚举类型返回枚举对象
     * 
     * @param e
     * @return
     */
    public static <T extends Enum<T>> T enumOf(Class<T> clazz, String name) {
        if (name == null) {
            return null;
        }
        return Enum.valueOf(clazz, name);
    }
}
