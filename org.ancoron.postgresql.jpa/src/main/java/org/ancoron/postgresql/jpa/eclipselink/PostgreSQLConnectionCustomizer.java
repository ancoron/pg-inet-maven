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
package org.ancoron.postgresql.jpa.eclipselink;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.databaseaccess.ConnectionCustomizer;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import static org.ancoron.postgresql.jpa.eclipselink.ExtendedPostgreSQLPlatform.*;

/**
 *
 * @author ancoron
 */
public class PostgreSQLConnectionCustomizer extends ConnectionCustomizer {

    private static final String CLASSNAME = "PostgreSQLConnectionCustomizer";
    private static final Logger log = Logger.getLogger(PostgreSQLConnectionCustomizer.class.getName());
    
    private AbstractJdbc2Connection pconn = null;
    
    public PostgreSQLConnectionCustomizer(Accessor accessor, Session session) {
        super(accessor, session);
    }

    @Override
    public void customize() {
        Connection conn = accessor.getConnection();
        
        if(conn != null) {
            if(conn instanceof AbstractJdbc2Connection) {
                // non-server mode...
                pconn = (AbstractJdbc2Connection) conn;
            } else {
                // try to unwrap...
                conn = session.getServerPlatform().unwrapConnection(conn);
                if(conn instanceof AbstractJdbc2Connection) {
                    pconn = (AbstractJdbc2Connection) conn;
                } else if(conn != null) {
                    log.logp(Level.WARNING, CLASSNAME, "customize",
                            "Unwrapped JDBC Connection is not an expected one: {0}",
                            conn.getClass().getName());
                } else {
                    log.logp(Level.WARNING, CLASSNAME, "customize",
                            "Unable to unwrap JDBC Connection");
                }
            }
        }
        
        if(pconn != null) {
            try {
                pconn.addDataType("CIDR", CIDR);
                pconn.addDataType("INET", INET);
                pconn.addDataType("MACADDR", MACADDR);

                log.logp(Level.FINE, CLASSNAME, "customize",
                        "JDBC Connection prepared for PostgreSQL specific network data types");
            } catch (SQLException ex) {
                log.logp(Level.WARNING, CLASSNAME, "customize",
                        "Unable to add networking extensions", ex);
            }
        }
    }

    @Override
    public boolean isActive() {
        return pconn != null;
    }

    @Override
    public void clear() {
        // nothing special to do here...
        pconn = null;
    }
}
