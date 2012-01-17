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
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 *
 * @author ancoron
 */
public class PostgreSQLDataSourceFactory implements DataSourceFactory {
    
    private final BundleContext context;
    
    protected PostgreSQLDataSourceFactory(BundleContext context) {
        this.context = context;
    }

    @Override
    public DataSource createDataSource(Properties prprts) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties prprts) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XADataSource createXADataSource(Properties prprts) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Driver createDriver(Properties prprts) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
