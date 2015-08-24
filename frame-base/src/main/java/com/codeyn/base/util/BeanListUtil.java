package com.codeyn.base.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;

public class BeanListUtil {
    public static List<T> copyList(List<?> source, Class<T> t) {
        List<T> retList = new ArrayList<T>();
        for (Object object : source) {
            try {
                T newInstance = t.newInstance();
                BeanUtils.copyProperties(object, newInstance);
                retList.add(newInstance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return retList;
    }
}
