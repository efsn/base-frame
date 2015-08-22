package com.codeyn.base.log;

/**
 * 在当前线程中缓存logToken
 * 
 */
public class LogTokenHolder {

    private static ThreadLocal<String> currentToken = new ThreadLocal<>();
    private static ThreadLocal<String> currentRequestId = new ThreadLocal<>();

    public static void init() {
        init(null);
    }

    public static void init(String token) {
        initRequestId();
        if (token != null) {
            currentToken.set(token);
        }
    }

    public static String getLogToken() {
        return currentToken.get();
    }

    private static void initRequestId() {
        // 取后6位
        String nanoToken = String.valueOf(System.nanoTime() % 1000000);
        // 取第4位开始到结束
        String msToken = String.valueOf(System.currentTimeMillis()).substring(4);
        // 这个数字应该在10多天范围内不会出现重复
        String rid = msToken + nanoToken;
        currentRequestId.set(rid);
    }

    public static String getRequestId() {
        return currentRequestId.get();
    }

    public static void clear() {
        currentRequestId.remove();
        currentToken.remove();
    }
}
