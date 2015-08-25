package com.codeyn.base.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Map生成辅助工具类 <br/>
 * 创建HashMap：<br/>
 * HashMap<String,String> map = MapUtil.build().add("userId", userId)
 * .add("userName", userName) .add("memberId", memberId).toHashMap(); <br/>
 * 创建LinkedHashMap：<br/>
 * LinkedHashMap<String,String> map = MapUtil.build().add("userId", userId)
 * .add("userName", userName) .add("memberId", memberId).toLinkedHashMap(); <br/>
 * 创建TreeMap：<br/>
 * TreeMap<String,String> map = MapUtil.build().add("userId", userId)
 * .add("userName", userName) .add("memberId", memberId).toTreeMap(); <br/>
 * 自定义Map泛型<br/>
 * HashMap<Foo,Bar> map = MapUtil.build(Foo.class,Bar.class).add(foo,
 * bar).toHashMap(); <br/>
 * 
 */
public class MapUtil {

    public static MapBuilder<String, String> build() {
        return build(String.class, String.class);
    }

    public static <K, V> MapBuilder<K, V> build(Class<K> keyType, Class<V> valueType) {
        MapBuilder<K, V> mapBuilder = new MapBuilder<>();
        return mapBuilder;
    }

    public static class MapBuilder<K, V> {

        private LinkedHashMap<K, V> cache = new LinkedHashMap<K, V>();

        public MapBuilder<K, V> add(K key, V value) {
            cache.put(key, value);
            return this;
        }

        public MapBuilder<K, V> addAll(Map<K, V> map) {
            if (map != null) {
                cache.putAll(map);
            }
            return this;
        }

        public HashMap<K, V> toHashMap() {
            HashMap<K, V> map = new HashMap<>();
            map.putAll(cache);
            return map;
        }

        public TreeMap<K, V> toTreeMap() {
            TreeMap<K, V> map = new TreeMap<>();
            map.putAll(cache);
            return map;
        }

        public LinkedHashMap<K, V> toLinkedHashMap() {
            LinkedHashMap<K, V> map = new LinkedHashMap<>();
            map.putAll(cache);
            return map;
        }
    }

}
