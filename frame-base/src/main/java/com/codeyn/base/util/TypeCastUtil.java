package com.codeyn.base.util;

import java.lang.reflect.Type;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

public class TypeCastUtil {

    public static <T> T cast(Object obj, Type type) {
        return TypeUtils.cast(obj, type, ParserConfig.getGlobalInstance());
    }

}
