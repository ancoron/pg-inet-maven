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
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import org.osgi.framework.BundleContext;
import org.postgresql.PGConnection;

/**
 *
 * @author ancoron
 */
public class DelegatingXADataSource implements XADataSource {
    
    private final XADataSource delegate;
    private boolean enhance = true;
    
    private Class clsPGinet = null;
    private Class clsPGcidr = null;
    private Class clsPGmacaddr = null;

    public DelegatingXADataSource(XADataSource delegate, BundleContext ctx) {
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
    
    public XAConnection getXAConnection() throws SQLException {
        return new DelegatingXAConnection(delegate.getXAConnection());
    }
    
    public XAConnection getXAConnection(String username, String password) throws SQLException {
        return new DelegatingXAConnection(delegate.getXAConnection(username, password));
    }

    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    class DelegatingXAConnection implements XAConnection {
        
        private final XAConnection delegate;

        public DelegatingXAConnection(XAConnection delegate) {
            this.delegate = delegate;
        }
        
        private void enhance(final Connection conn) throws SQLException {
            if(enhance && conn != null && conn instanceof PGConnection) {
                final PGConnection pc = (PGConnection) conn;
                pc.addDataType("INET", clsPGinet);
                pc.addDataType("CIDR", clsPGcidr);
                pc.addDataType("MACADDR", clsPGmacaddr);
            }
        }

        public Connection getConnection() throws SQLException {
            Connection conn = delegate.getConnection();
            
            enhance(conn);
            
            return conn;
        }

        public void close() throws SQLException {
            delegate.close();
        }

        public void addConnectionEventListener(ConnectionEventListener listener) {
            delegate.addConnectionEventListener(listener);
        }

        public void removeConnectionEventListener(ConnectionEventListener listener) {
            delegate.removeConnectionEventListener(listener);
        }

        public void addStatementEventListener(StatementEventListener listener) {
            delegate.addStatementEventListener(listener);
        }

        public void removeStatementEventListener(StatementEventListener listener) {
            delegate.removeStatementEventListener(listener);
        }

        public XAResource getXAResource() throws SQLException {
            return delegate.getXAResource();
        }
    }
}
