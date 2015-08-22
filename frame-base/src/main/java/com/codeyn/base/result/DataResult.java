package com.codeyn.base.result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.codeyn.base.util.TypeCastUtil;

public class DataResult extends BaseResult {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public DataResult put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public DataResult putAll(Map<String, Object> map) {
        data.putAll(map);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return TypeCastUtil.<T> cast(data.get(key), clazz);
    }

    public String getString(String key) {
        return this.get(key, String.class);
    }

    public Integer getInt(String key) {
        return this.get(key, Integer.class);
    }

    public Long getLong(String key) {
        return this.get(key, Long.class);
    }

    public Double getDouble(String key) {
        return this.get(key, Double.class);
    }

    public Float getFloat(String key) {
        return this.get(key, Float.class);
    }

    public Boolean getBoolean(String key) {
        return this.get(key, Boolean.class);
    }

    public Byte getByte(String key) {
        return this.get(key, Byte.class);
    }

    public Character getChar(String key) {
        return this.get(key, Character.class);
    }

    public Short getShort(String key) {
        return this.get(key, Short.class);
    }

    public BigDecimal getBigDecimal(String key) {
        return this.get(key, BigDecimal.class);
    }

    public BigInteger getBigInteger(String key) {
        return this.get(key, BigInteger.class);
    }

    public Date getDate(String key) {
        return this.get(key, Date.class);
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("DataResult [isSuccess=").append(isSuccess).append(", code=").append(code).append(", msg=")
                .append(msg).append(", data=").append(data != null ? toString(data.entrySet(), maxLen) : null)
                .append("]");
        return builder.toString();
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

}
