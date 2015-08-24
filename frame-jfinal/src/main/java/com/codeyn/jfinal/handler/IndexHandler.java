package com.codeyn.jfinal.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.render.RedirectRender;

public class IndexHandler extends Handler {

    private String path;
    private Set<String> redirectPaths;

    public IndexHandler(String path) {
        this.path = path;
        redirectPaths = new HashSet<>(Arrays.asList("", "/"));
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        if (path != null && redirectPaths.contains(target) && !target.equals(path)) {
            new RedirectRender(path).setContext(request, response).render();
            isHandled[0] = true;
            return;
        }
        nextHandler.handle(target, request, response, isHandled);
    }

}
