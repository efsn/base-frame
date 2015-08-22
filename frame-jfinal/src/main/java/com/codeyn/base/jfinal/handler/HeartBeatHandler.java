package com.codeyn.base.jfinal.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.jfinal.render.TextRender;

public class HeartBeatHandler extends Handler {

    private static final Log logger = LogFactory.getLog(HeartBeatHandler.class);
    
    private static final String DEFAULT_URI = "/heartbeat";
    
    private String uri;
    
    public HeartBeatHandler(){
        this.uri = DEFAULT_URI;
    }
    
    public HeartBeatHandler(String uri){
        this.uri = StringUtils.isBlank(uri) ? DEFAULT_URI : uri;
    }
    
    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled){
        if(JFinal.me().getConstants().getDevMode()){
            logger.debug("Heart beat try uri: " + target);
        }
        
        if(uri.equals(target)){
            new TextRender("Success").setContext(request, response).render();
            isHandled[0] = true;
            return;
        }
        nextHandler.handle(target, request, response, isHandled);
    }

}
