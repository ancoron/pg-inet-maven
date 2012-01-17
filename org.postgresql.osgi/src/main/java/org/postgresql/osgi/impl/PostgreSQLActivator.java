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

import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.postgresql.Driver;

/**
 *
 * @author ancoron
 */
public class PostgreSQLActivator implements BundleActivator {
    
    public void start(BundleContext context) throws Exception {
        DataSourceFactory factory = new PostgreSQLDataSourceFactory(context);

        Properties serviceProperties = new Properties();

        serviceProperties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS,
                org.postgresql.Driver.class.getName());

        serviceProperties.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION,
                Driver.getVersion());

        serviceProperties.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME,
                "PostgreSQL");

        context.registerService(DataSourceFactory.class.getName(),
                factory, serviceProperties);
    }

    public void stop(BundleContext context) throws Exception {
        // nothing to be done here...
    }
    
}
