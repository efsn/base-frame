package com.codeyn.jfinal.interceptor;

import com.codeyn.jfinal.base.BaseController;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

public class PostInterceptor implements Interceptor{

    @Override
    public void intercept(Invocation inv){

        Controller c = inv.getController();
        if(c != null && c instanceof BaseController){
            ((BaseController)c).afterInterceptor();
        }
        inv.invoke();
    }

}
