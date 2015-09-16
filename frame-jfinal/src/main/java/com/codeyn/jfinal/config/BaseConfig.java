package com.codeyn.jfinal.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codeyn.base.util.PackageScanner;
import com.codeyn.base.util.PackageScanner.ClassFilter;
import com.codeyn.jfinal.annos.JFinalAnnos.ModelMapping;
import com.codeyn.jfinal.annos.JFinalAnnos.Route;
import com.codeyn.jfinal.handler.HeartBeatHandler;
import com.codeyn.jfinal.handler.IndexHandler;
import com.codeyn.jfinal.handler.PreHandler;
import com.codeyn.jfinal.interceptor.ExceptionInterceptor;
import com.codeyn.jfinal.interceptor.PostInterceptor;
import com.codeyn.jfinal.plugins.spring.IocInterceptor;
import com.codeyn.jfinal.plugins.spring.SpringPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.FreeMarkerRender;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

/**
 * Base framework configuration
 * 
 * @author Codeyn
 *
 */
public abstract class BaseConfig extends JFinalConfig {

    private ApplicationContext ctx;
    private List<ActiveRecordPlugin> arps;

    private IndexHandler home;
    private PreHandler preHandler;
    private ExceptionInterceptor ei;
    private boolean devMode;

    public BaseConfig() {
        ei = new ExceptionInterceptor();
        preHandler = new PreHandler();
        arps = new ArrayList<>();
        init();
    }

    @Override
    public void configConstant(Constants me) {
        onConfigConstant(me);
        this.devMode = me.getDevMode();

        // TODO I18N

        // TODO customizer Logger (ILoggerFacotry/wrapper Logger)

        // TODO Nginx Redis Ehcache session(default) cache synchronized
        me.setTokenCache(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configRoute(Routes me) {
        Set<Class<?>> classes = PackageScanner.scanPackage(new ClassFilter() {

            @Override
            public boolean access(Class<?> clazz) {
                return Controller.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Route.class);
            }

        }, "");

        for (Class<?> clazz : classes) {
            Route route = clazz.getAnnotation(Route.class);
            if (StringUtils.isBlank(route.viewPath())) {
                me.add(route.value(), (Class<? extends Controller>) clazz);
            } else {
                me.add(route.value(), (Class<? extends Controller>) clazz, route.viewPath());
            }
        }

        onConfigRoute(me);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void configPlugin(Plugins me) {
        if (ctx != null) {
            me.add(new SpringPlugin(ctx));
        }

        // default mysql datasource
        C3p0Plugin c3p0 = new C3p0Plugin(prop.getProperties());
        me.add(c3p0);
        ActiveRecordPlugin mysql = new ActiveRecordPlugin(c3p0);
        Set<Class<?>> classes = PackageScanner.scanPackage(new ClassFilter() {

            @Override
            public boolean access(Class<?> clazz) {
                return Model.class.isAssignableFrom(clazz) && Model.class != clazz
                        && clazz.isAnnotationPresent(ModelMapping.class);
            }

        }, "");

        for (Class<?> clazz : classes) {
            ModelMapping mm = clazz.getAnnotation(ModelMapping.class);
            if (StringUtils.isBlank(mm.primary())) {
                mysql.addMapping(mm.value(), (Class<? extends Model<?>>) clazz);
            } else {
                mysql.addMapping(mm.value(), mm.primary(), (Class<? extends Model<?>>) clazz);
            }
        }
        me.add(mysql);

        // something else
        if (arps != null) {
            for (ActiveRecordPlugin arp : arps) {
                me.add(arp);
            }
        }
        onConfigPlugin(me);
    }
    
    @Override
    public void configInterceptor(Interceptors me) {
        if (ctx != null) {
            me.add(new IocInterceptor());
        }
        ei.setDevMode(devMode);
        me.add(ei);
        onConfigInterceptor(me);
        me.add(new PostInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {
        // 心跳检测接口
        me.add(new HeartBeatHandler());

        // 后缀处理器
        me.add(preHandler);

        if (home != null) {
            me.add(home);
        }
        onConfigHandler(me);
    }

    public void addArp(ActiveRecordPlugin arp) {
        arps.add(arp);
    }

    public void initSpringCtx(String... xmls) {
        ctx = new ClassPathXmlApplicationContext(xmls);
    }

    public ApplicationContext getSpringCtx() {
        return ctx;
    }

    public void addJsonExts(String... exts) {
        ei.addJsonExts(exts);
    }

    public void addActionExts(String... exts) {
        preHandler.addExts(exts);
    }

    public void setIndexPath(String path) {
        if (path != null) {
            home = new IndexHandler(path);
        }
    }
    
    protected void setSharedVariable(Set<String> packages) {
        Set<Class<?>> clazzs = PackageScanner.scanPackage(packages.toArray(new String[0]));
        for (Class<?> clazz : clazzs) {
            TemplateHashModel temp = buildStaticTemplate(clazz.getName());
            FreeMarkerRender.getConfiguration().setSharedVariable(clazz.getSimpleName(), temp);
        }
    }
    
    protected TemplateHashModel buildStaticTemplate(String name) {
        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
        TemplateHashModel tmp = wrapper.getStaticModels();
        try {
            return (TemplateHashModel) tmp.get(name);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onConfigHandler(Handlers me) {

    }

    protected void onConfigInterceptor(Interceptors me) {

    }

    protected void onConfigPlugin(Plugins me) {

    }

    protected void onConfigConstant(Constants me) {

    }

    protected void onConfigRoute(Routes me) {

    }

    protected abstract void init();

}
