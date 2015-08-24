package com.codeyn.resouce.bus.ds;

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.druid.pool.DruidDataSource;
import com.codeyn.resouce.bus.ConfigLoader;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DriverManagerDataSource;

/**
 * DataSource factory
 * 
 * @author Codeyn
 *
 */
public class DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    public static DruidDataSource buildDruidDs(String dsName) {
        return buildDruidDs(dsName, null, null);
    }

    public static DruidDataSource buildDruidDs(String dsName, Properties connectionProp, Properties dsProp) {
        DataSourceConfig dsc = buildDsCfg(dsName);
        DruidDataSource dds = new DruidDataSource();

        dds.setUrl(dsc.getUrl());
        dds.setDriverClassName(dsc.getDriverClass());
        dds.setUsername(dsc.getUser());
        dds.setPassword(dsc.getPassword());
        dds.setConnectProperties(connectionProp);
        copyDsProp(dds, dsProp);
        return dds;
    }

    public static ComboPooledDataSource buildC3p0Ds(String dsName) {
        return buildC3p0Ds(dsName, null, null);
    }

    public static ComboPooledDataSource buildC3p0Ds(String dsName, Properties connectionProp, Properties dsProp) {
        DataSourceConfig dsc = buildDsCfg(dsName);
        ComboPooledDataSource ds = new ComboPooledDataSource();

        try {
            ds.setDriverClass(dsc.getDriverClass());
        } catch (PropertyVetoException e) {
            throw new RuntimeException("build database error", e);
        }
        ds.setJdbcUrl(dsc.getUrl());
        ds.setUser(dsc.getUser());
        ds.setPassword(dsc.getPassword());

        ds.setMaxIdleTime(60);
        ds.setAcquireIncrement(1);
        ds.setIdleConnectionTestPeriod(60);
        ds.setPreferredTestQuery("SELECT 1 FROM DUAL");
        ds.setTestConnectionOnCheckin(true);
        ds.setTestConnectionOnCheckout(false);
        ds.setAcquireRetryAttempts(10);

        if (connectionProp != null) {
            ds.setProperties(connectionProp);
        }

        copyDsProp(ds, dsProp);
        return ds;
    }

    public static DriverManagerDataSource buildSimpleDs(String dsName) {
        return buildSimpleDs(dsName, null, null);
    }

    public static DriverManagerDataSource buildSimpleDs(String dsName,
                                                                Properties connectionProp,
                                                                Properties dsProp) {
        DataSourceConfig dsc = buildDsCfg(dsName);
        DriverManagerDataSource ds = new DriverManagerDataSource();

        ds.setDriverClass(dsc.getDriverClass());
        ds.setJdbcUrl(dsc.getUrl());
        ds.setUser(dsc.getUser());
        ds.setPassword(dsc.getPassword());

        if (connectionProp != null) {
            ds.setProperties(connectionProp);
        }

        copyDsProp(ds, dsProp);
        return ds;
    }

    public static DataSource buildDsByType(DataSourceType type, String dsName) {
        return buildDsByType(type, dsName, null, null);
    }

    public static DataSource buildDsByType(DataSourceType type,
                                           String dsName,
                                           Properties connectionProp,
                                           Properties dsProp) {
        switch(type){
            case DRUID:
                return buildDruidDs(dsName, connectionProp, dsProp);
                
            case C3P0:
                return buildC3p0Ds(dsName, connectionProp, dsProp);
                
            case SIMPLE:
                return buildSimpleDs(dsName, connectionProp, dsProp);
                
            default:
                    return buildDruidDs(dsName, connectionProp, dsProp);
        }
    }
    
    private static DataSourceConfig buildDsCfg(String dsName) {
        Assert.hasText(dsName, "dsName must not be empty");
        DataSourceConfig dsc = ConfigLoader.getDsCfg(dsName);
        if (dsc == null) {
            throw new IllegalArgumentException("no database setting for name '".concat(dsName).concat("'"));
        } else {
            logger.info("load database config success, dsName:{}, {}", dsName, dsc);
        }
        if (dsc.getDriverClass() == null) {
            // default is mysql
            dsc.setDriverClass("com.mysql.jdbc.Driver");
        }
        return dsc;
    }

    private static void copyDsProp(DataSource ds, Properties dsProp) {
        if (dsProp != null) {
            try {
                BeanUtils.copyProperties(ds, dsProp);
            } catch (Exception e) {
                logger.error("build data source error", e);
            }
        }
    }

}
