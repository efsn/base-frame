package com.codeyn.base.jfinal.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;

public class IndexHandler extends Handler{

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled){
        System.out.println("index handler");
        isHandled[0] = true;
    }

}
