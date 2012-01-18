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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.osgi.framework.BundleContext;
import org.postgresql.PGConnection;

/**
 *
 * @author ancoron
 */
public class DelegatingDataSource implements DataSource {
    
    private final DataSource delegate;
    private boolean enhance = true;
    
    private Class clsPGinet = null;
    private Class clsPGcidr = null;
    private Class clsPGmacaddr = null;

    public DelegatingDataSource(DataSource delegate, BundleContext ctx) {
        this.delegate = delegate;

        // try to load network extension...
        try {
            clsPGinet = ctx.getBundle().loadClass("org.postgresql.net.PGinet");
            clsPGcidr = ctx.getBundle().loadClass("org.postgresql.net.PGcidr");
            clsPGmacaddr = ctx.getBundle().loadClass("org.postgresql.net.PGmacaddr");
            
            enhance = true;
        } catch(ClassNotFoundException x) {
            enhance = false;
        }
    }
    
    private void enhance(final Connection conn) throws SQLException {
        if(enhance && conn != null && conn instanceof PGConnection) {
            final PGConnection pc = (PGConnection) conn;
            pc.addDataType("INET", clsPGinet);
            pc.addDataType("CIDR", clsPGcidr);
            pc.addDataType("MACADDR", clsPGmacaddr);
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = delegate.getConnection();
        
        enhance(conn);
        
        return conn;
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = delegate.getConnection(username, password);
        
        enhance(conn);
        
        return conn;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
    
}
