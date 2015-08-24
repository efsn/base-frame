package com.codeyn.jfinal.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import com.codeyn.jfinal.annos.JFinalAnnos.ModelMapping;
import com.codeyn.resouce.bus.ds.DataSourceFactory;
import com.codeyn.resouce.bus.ds.DataSourceType;
import com.codeyn.utils.PackageScanner;
import com.codeyn.utils.PackageScanner.ClassFilter;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Model;

public class ArpBuilder {

    private String dsName;
    private String cfgName;
    private DataSource ds;
    private DataSourceType type = DataSourceType.DRUID;

    private Properties connectionProp;
    private Properties dsProp;

    private Class<? extends Model<?>>[] clazzes;

    private String[] modelPackages;
    private DsPostHandler postHandler;

    public ArpBuilder(String dsName) {
        if (StringUtils.isBlank(dsName)) {
            throw new IllegalArgumentException("dsName can not be empty");
        }
        this.dsName = dsName;
        this.cfgName = dsName;
    }

    public ArpBuilder(String dsName, DataSourceType type) {
        this(dsName);
        this.type = type;
    }

    public ArpBuilder(String dsName, DataSource ds) {
        this(dsName);
        this.ds = ds;
    }

    public ArpBuilder setCfgName(String cfgName) {
        this.cfgName = cfgName;
        return this;
    }

    public ArpBuilder configure(Properties connectionProp, Properties dsProp) {
        this.connectionProp = connectionProp;
        this.dsProp = dsProp;
        return this;
    }

    @SuppressWarnings("unchecked")
    public ArpBuilder mapping(Class<? extends Model<?>>... clazzs) {
        for (Class<? extends Model<?>> clazz : clazzs) {
            if (!clazz.isAnnotationPresent(ModelMapping.class)) {
                StringBuffer sb = new StringBuffer("model mapping class '").append(clazz.getName()).append(
                        "' required annotation 'ModelMapping'");
                throw new IllegalArgumentException(sb.toString());
            }
        }
        this.clazzes = clazzs;
        return this;
    }

    public ArpBuilder mapping(String... modelPackages) {
        this.modelPackages = modelPackages;
        return this;
    }

    public ArpBuilder afterDsInit(DsPostHandler postHandler) {
        this.postHandler = postHandler;
        return this;
    }

    public ActiveRecordPlugin build() {
        if (ds == null) {
            ds = DataSourceFactory.buildDsByType(type, dsName, connectionProp, dsProp);
            if (postHandler != null) {
                postHandler.postHandle(ds);
            }
        }

        ActiveRecordPlugin arp = new ActiveRecordPlugin(cfgName, ds);
        arp.setShowSql(true);
        initModelMapping(arp);
        return arp;
    }

    @SuppressWarnings("unchecked")
    private void initModelMapping(ActiveRecordPlugin arp) {
        Set<Class<? extends Model<?>>> set = clazzes == null ? new LinkedHashSet<Class<? extends Model<?>>>()
                : new LinkedHashSet<>(Arrays.asList(clazzes));
        if (modelPackages != null && modelPackages.length > 0) {
            Set<Class<?>> clazzes = PackageScanner.scanPackage(new ClassFilter() {

                @Override
                public boolean access(Class<?> clazz) {
                    return Model.class.isAssignableFrom(clazz) && Model.class != clazz
                            && clazz.isAnnotationPresent(ModelMapping.class);
                }

            }, modelPackages);
            for (Class<?> clazz : clazzes) {
                set.add((Class<? extends Model<?>>) clazz);
            }
        }

        for (Class<? extends Model<?>> clazz : set) {
            ModelMapping mm = clazz.getAnnotation(ModelMapping.class);
            String tb = mm.value();
            String pk = mm.primary();
            if (StringUtils.isNotBlank(pk)) {
                arp.addMapping(tb, pk, clazz);
            } else {
                arp.addMapping(tb, clazz);
            }
        }
    }
    
    public interface DsPostHandler {
        void postHandle(DataSource ds);
    }

}
