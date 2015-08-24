package com.codeyn.jfinal.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;

/**
 * Uri suffix filter
 * 
 * @author Codeyn
 *
 */
public class PreHandler extends Handler {

    private static String DOT = ".";
    private final String[] defaults = {"json", "html", "do"};

    private Set<String> suffixs;

    public PreHandler() {
        suffixs = new HashSet<>();
        suffixs.addAll(Arrays.asList(defaults));
    }

    public void addExts(String... exts) {
        if (exts != null) {
            suffixs.addAll(Arrays.asList(exts));
        }
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        int idx = target.indexOf(DOT);
        if (idx > -1) {
            String ext = target.substring(idx + 1);
            if (suffixs.contains(ext)) {
                target = target.substring(0, idx);
            }
        }
        nextHandler.handle(target, request, response, isHandled);
    }

}
