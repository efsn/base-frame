package com.codeyn.base.common;

import java.util.Collection;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.codeyn.base.exception.BusinessException;

public class Assert {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    public static void isNull(Object object, String name) {
        if (object != null) {
            throw new BusinessException("Argument '" + name + "' must be null!");
        }
    }

    public static void notNull(Object object, String name) {
        if (object == null) {
            throw new BusinessException("Argument '" + name + "' must not be null!");
        }
    }

    public static void hasText(String text, String name) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("Argument '" + name + "' must not be empty!");
        }
    }

    public static void notEmpty(Object[] array, String name) {
        if (ObjectUtils.isEmpty(array)) {
            throw new BusinessException("Array '" + name + "' must not be empty!");
        }
    }

    public static void notEmpty(Collection<?> collection, String name) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException("Collection '" + name + "' must not be empty!");
        }
    }

}
