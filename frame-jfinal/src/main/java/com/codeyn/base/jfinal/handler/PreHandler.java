package com.codeyn.base.jfinal.handler;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jfinal.handler.Handler;

/**
 * Uri suffix filter 
 * @author Arthur
 *
 */
public class PreHandler extends Handler{

    private static String DOT = ".";
    private final List<String> suffixs = Arrays.asList("json", "html");
    
    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled){
        if(StringUtils.isBlank(target) || target.indexOf(DOT) == -1){
            nextHandler.handle(target, request, response, isHandled);
            return;
        }
        int idx = target.indexOf(DOT);
        String ext = target.substring(idx + 1);
        if(suffixs.contains(ext)){
            nextHandler.handle(target.substring(0, idx), request, response, isHandled);
        }
    }

}
