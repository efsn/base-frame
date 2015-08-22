package com.codeyn.jfinal.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class ExceptionInterceptor implements Interceptor{

    @Override
    public void intercept(Invocation inv){
        inv.invoke();
    }

}
