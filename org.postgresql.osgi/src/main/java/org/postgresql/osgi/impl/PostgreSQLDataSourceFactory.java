/*
 * Copyright 2012 ancoron.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.postgresql.osgi.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.osgi.framework.BundleContext;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.ds.common.BaseDataSource;
import org.postgresql.osgi.PGDataSourceFactory;
import org.postgresql.xa.PGXADataSource;

/**
 *
 * @author ancoron
 */
public class PostgreSQLDataSourceFactory implements PGDataSourceFactory {
    
    private static final Set poolingKeys = new HashSet();
    
    private final BundleContext context;
    
    static {
        poolingKeys.add(JDBC_INITIAL_POOL_SIZE);
        poolingKeys.add(JDBC_MAX_POOL_SIZE);
        poolingKeys.add(JDBC_MIN_POOL_SIZE);
        poolingKeys.add(JDBC_MAX_IDLE_TIME);
        poolingKeys.add(JDBC_MAX_STATEMENTS);
        poolingKeys.add(JDBC_PROPERTY_CYCLE);
    }
    
    protected PostgreSQLDataSourceFactory(BundleContext context) {
        this.context = context;
    }
    
    private void setBaseDSProperties(final BaseDataSource ds,
            final Properties props) throws SQLException
    {
        try {
            for(Iterator it = props.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = props.getProperty(key);

                if(JDBC_DATABASE_NAME.equals(key)) {
                    ds.setDatabaseName(value);
                } else if(JDBC_SERVER_NAME.equals(key)) {
                    ds.setServerName(value);
                } else if(JDBC_PORT_NUMBER.equals(key)) {
                    ds.setPortNumber(Integer.valueOf(value).intValue());
                } else if(JDBC_USER.equals(key)) {
                    ds.setUser(value);
                } else if(JDBC_PASSWORD.equals(key)) {
                    ds.setPassword(value);
                } else if(JDBC_PG_COMPATIBLE.equals(key)) {
                    ds.setCompatible(value);
                } else if(JDBC_PG_LOGIN_TIMEOUT_IN_SECONDS.equals(key)) {
                    ds.setLoginTimeout(Integer.valueOf(value).intValue());
                } else if(JDBC_PG_SOCKET_TIMEOUT_IN_SECONDS.equals(key)) {
                    ds.setSocketTimeout(Integer.valueOf(value).intValue());
                } else if(JDBC_PG_SSL.equals(key)) {
                    ds.setSsl(Boolean.valueOf(value).booleanValue());
                } else if(JDBC_PG_SSL_FACTORY.equals(key)) {
                    ds.setSslfactory(value);
                } else if(JDBC_PG_STATEMENT_PREPARE_THRESHOLD.equals(key)) {
                    ds.setPrepareThreshold(Integer.valueOf(value).intValue());
                } else if(JDBC_PG_TCP_KEEPALIVE.equals(key)) {
                    ds.setTcpKeepAlive(Boolean.valueOf(value).booleanValue());
                } else if(JDBC_PG_UNKNOWN_LENGTH.equals(key)) {
                    ds.setUnknownLength(Integer.valueOf(value).intValue());
                } else {
                    throw new SQLException("RFC #122 - Property '" + key + "' not (yet) supported");
                }
            }
        } catch(Exception x) {
            if(x instanceof SQLException) {
                throw (SQLException) x;
            }
            throw new SQLException(x);
        }
    }
    
    private void setPooledDSProperties(final PGConnectionPoolDataSource ds,
            final Properties props) throws SQLException
    {
        ds.setDefaultAutoCommit(false);
        
        if(props == null) {
            return;
        }
        
        try {
            for(Iterator it = props.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                if(poolingKeys.contains(key)) {
                    throw new SQLException("RFC #122 - Property '" + key + "' not (yet) supported");
                }
            }
        } catch(Exception x) {
            if(x instanceof SQLException) {
                throw (SQLException) x;
            }
            throw new SQLException(x);
        }
    }

    public DataSource createDataSource(Properties prprts) throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        setBaseDSProperties(ds, prprts);
        
        return ds;
    }

    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties prprts) throws SQLException {
        PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
        
        setBaseDSProperties(ds, prprts);
        setPooledDSProperties(ds, prprts);
        
        return ds;
    }

    public XADataSource createXADataSource(Properties prprts) throws SQLException {
        PGXADataSource ds = new PGXADataSource();
        
        setBaseDSProperties(ds, prprts);
        
        return ds;
    }

    public Driver createDriver(Properties prprts) throws SQLException {
        try {
            Driver drv = new org.postgresql.Driver();
            
            // ensure the driver is registered...
            Class.forName(drv.getClass().getName(), false, getClass().getClassLoader());
            
            return drv;
        } catch (ClassNotFoundException x) {
            throw new SQLException(x);
        }
    }
}
