package com.codeyn.jfinal.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.codeyn.jfinal.annos.JFinalAnnos.ModelMapping;
import com.codeyn.jfinal.annos.JFinalAnnos.Route;
import com.codeyn.jfinal.handler.HeartBeatHandler;
import com.codeyn.jfinal.handler.IndexHandler;
import com.codeyn.jfinal.handler.PreHandler;
import com.codeyn.jfinal.interceptor.ExceptionInterceptor;
import com.codeyn.utils.PackageScanner;
import com.codeyn.utils.PackageScanner.ClassFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Const;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.c3p0.C3p0Plugin;

/**
 * Base framework configuration
 * 
 * @author Arthur
 * @version 1.0
 *
 */
public abstract class BaseConfig extends JFinalConfig{

    private IndexHandler home;
    private ExceptionInterceptor ei;
    private List<ActiveRecordPlugin> arps;
    
    public BaseConfig(){
        ei = new ExceptionInterceptor();
        arps = new ArrayList<>();
        init();
    }
    
    @Override
    public void configConstant(Constants me){
        me.setDevMode(PropKit.getBoolean("devModel"));
        me.setEncoding(Const.DEFAULT_ENCODING);
        
        // TODO I18N

        // TODO customizer Logger (ILoggerFacotry/wrapper Logger)
        
        // TODO Nginx Redis Ehcache session(default) cache synchronized
        me.setTokenCache(null);
        
        onConfigConstant(me);
//        devMode = me.getDevMode();
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configRoute(Routes me){
        Set<Class<?>> classes = PackageScanner.scanPackage(new ClassFilter(){

            @Override
            public boolean access(Class<?> clazz){
                return Controller.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Route.class);
            }
            
        }, "com.codeyn.*.controller");
        
        for(Class<?> clazz : classes){
            Route route = clazz.getAnnotation(Route.class);
            if(StringUtils.isBlank(route.viewPath())){
                me.add(route.value(), (Class<? extends Controller>)clazz);
            }else{
                me.add(route.value(), (Class<? extends Controller>)clazz, route.viewPath());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configPlugin(Plugins me){
        // default mysql datasource
        C3p0Plugin c3p0 = new C3p0Plugin(prop.getProperties());
        me.add(c3p0);
        ActiveRecordPlugin mysql = new ActiveRecordPlugin(c3p0);
        Set<Class<?>> classes = PackageScanner.scanPackage(new ClassFilter(){

            @Override
            public boolean access(Class<?> clazz){
                return Model.class.isAssignableFrom(clazz) && Model.class != clazz && clazz.isAnnotationPresent(ModelMapping.class);
            }
            
        }, "com.codeyn.*.model");
        
        for(Class<?> clazz : classes){
            ModelMapping mm = clazz.getAnnotation(ModelMapping.class);
            if(StringUtils.isBlank(mm.primary())){
                mysql.addMapping(mm.value(), (Class<? extends Model<?>>)clazz);
            }else{
                mysql.addMapping(mm.value(), mm.primary(), (Class<? extends Model<?>>)clazz);
            }
        }
        me.add(mysql);
        
        // something else
        if(arps != null){
            for(ActiveRecordPlugin arp : arps){
                me.add(arp);
            }
        }
        
        onConfigPlugin(me);
    }

    @Override
    public void configInterceptor(Interceptors me){
        me.add(ei);
        onConfigInterceptor(me);
    }

    @Override
    public void configHandler(Handlers me){
        me.add(new HeartBeatHandler());
        me.add(new PreHandler());
        if(home != null){
            me.add(home);
        }
        onConfigHandler(me);
    }
    
    protected void onConfigHandler(Handlers me){
        
    }
    
    protected void onConfigInterceptor(Interceptors me){
        
    }
    
    protected void onConfigPlugin(Plugins me){
        
    }
    
    protected void onConfigConstant(Constants me){
        
    }
    
    protected abstract void init();

}
