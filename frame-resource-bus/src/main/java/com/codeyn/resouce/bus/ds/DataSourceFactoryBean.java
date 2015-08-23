package com.codeyn.resouce.bus.ds;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
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
public class DataSourceFactoryBean implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactoryBean.class);

    private String dsName;
    private String dsType;
    private DataSource dsTarget;
    private Class<?> dsClazz;

    private String driverClassFiled = "driverClass";
    private String jdbcUrlFiled = "jdbcUrl";
    private String userFiled = "user";
    private String passwordFiled = "password";
    private String connectionPropsFiled = "properties";

    private Properties dsProps;
    private Properties connectionProps;

    public void setDsType(String dsType) {
        this.dsType = dsType;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public void setDriverClassFiled(String driverClassFiled) {
        this.driverClassFiled = driverClassFiled;
    }

    public void setJdbcUrlFiled(String jdbcUrlFiled) {
        this.jdbcUrlFiled = jdbcUrlFiled;
    }

    public void setUserFiled(String userFiled) {
        this.userFiled = userFiled;
    }

    public void setPasswordFiled(String passwordFiled) {
        this.passwordFiled = passwordFiled;
    }

    public void setConnectionPropsFiled(String connectionPropsFiled) {
        this.connectionPropsFiled = connectionPropsFiled;
    }

    public void setDsTarget(DataSource dsTarget) {
        this.dsTarget = dsTarget;
    }

    public void setDsProps(Properties dsProps) {
        this.dsProps = dsProps;
    }

    public void setConnectionProps(Properties connectionProps) {
        this.connectionProps = connectionProps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(dsName, "Argument 'dbName' can't be empty.");
        if (dsTarget != null) {
            dsClazz = dsTarget.getClass();
        } else {
            if (dsType == null) {
                dsType = "pool";
            }
            switch (dsType) {
                case "druid":
                    dsClazz = DruidDataSource.class;
                    dsTarget = DataSourceFactory.buildDruidDs(dsName, connectionProps, dsProps);
                    break;
                case "pool":
                case "c3p0":
                    dsClazz = ComboPooledDataSource.class;
                    dsTarget = DataSourceFactory.buildC3p0Ds(dsName, connectionProps, dsProps);
                    break;
                case "normal":
                case "simple":
                    dsClazz = DriverManagerDataSource.class;
                    dsTarget = DataSourceFactory.buildSimpleDs(dsName, connectionProps, dsProps);
                    break;
                default:
                    dsClazz = Class.forName(dsType);
                    if (!DataSource.class.isAssignableFrom(dsClazz)) {
                        throw new IllegalArgumentException("dsClass must be 'javax.sql.DataSource' or it's subclass");
                    }
                    dsTarget = (DataSource) dsClazz.newInstance();
                    initDataSource();
            }
        }

    }

    @Override
    public DataSource getObject() throws Exception {
        return dsTarget;
    }

    @Override
    public Class<?> getObjectType() {
        return dsClazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private void initDataSource() throws Exception {
        DataSourceConfig dsc = ConfigLoader.getDsCfg(dsName);
        if (dsc == null) {
            throw new IllegalArgumentException("no database settings for name '" + dsName + "'");
        } else {
            logger.info("load database info success. dsName: {}, {}", dsName, dsc);
        }
        BeanUtils.copyProperty(dsTarget, driverClassFiled, dsc.getDriverClass());
        BeanUtils.copyProperty(dsTarget, jdbcUrlFiled, dsc.getUrl());
        BeanUtils.copyProperty(dsTarget, userFiled, dsc.getUser());
        BeanUtils.copyProperty(dsTarget, passwordFiled, dsc.getPassword());
        if (connectionProps != null) {
            BeanUtils.copyProperty(dsTarget, connectionPropsFiled, connectionProps);
        }
        if (dsProps != null) {
            Map<Object, Object> map = new HashMap<>(dsProps);
            BeanUtils.copyProperties(dsTarget, map);
        }
    }

    public void close() {

    }

    @Override
    public void destroy() throws Exception {
        if (dsTarget instanceof ComboPooledDataSource) {
            ((ComboPooledDataSource) dsTarget).close();
        } else if (dsTarget instanceof Closeable) {
            ((Closeable) dsTarget).close();
        }
    }

}
