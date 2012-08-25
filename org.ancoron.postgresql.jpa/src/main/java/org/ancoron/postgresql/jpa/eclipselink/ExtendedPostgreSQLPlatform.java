/*
 * Copyright 2011-2012 ancoron.
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

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.IPTarget;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.databaseaccess.BindCallCustomParameter;
import org.eclipse.persistence.internal.databaseaccess.ConnectionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.eclipse.persistence.queries.Call;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.net.PGmacaddr;
import org.postgresql.util.PGobject;

/**
 *
 * @author ancoron
 */
public class ExtendedPostgreSQLPlatform extends PostgreSQLPlatform {

    private static final Logger log = Logger.getLogger(ExtendedPostgreSQLPlatform.class.getName());
    
    static final Class CIDR = PGcidr.class;
    static final Class INET = PGinet.class;
    static final Class MACADDR = PGmacaddr.class;
    
    private static final int TYPE_CIDR = 60001;
    private static final int TYPE_INET = 60002;
    private static final int TYPE_MAC = 60003;
    private static final int TYPE_NET = 60004;
    private static final int TYPE_IP = 60005;

    @Override
    protected void appendBoolean(Boolean bool, Writer writer) throws IOException {
        if (bool.booleanValue()) {
            writer.write("true");
        } else {
            writer.write("false");
        }
    }
    
    @Override
    public Object getObjectFromResultSet(ResultSet resultSet, int columnNumber, int type, AbstractSession session) throws SQLException {
        Object result = super.getObjectFromResultSet(resultSet, columnNumber, type, session);

        if(result != null) {
            if(type == TYPE_CIDR) {
                convertToCidr(result);
            } else if(type == TYPE_INET) {
                convertToInet(result);
            } else if(type == TYPE_MAC) {
                convertToMac(result);
            }
        }

        return result;
    }

    private PGmacaddr convertToMac(Object value) {
        PGmacaddr mac = null;
        if(value != null) {
            try {
                if(value instanceof PGmacaddr) {
                    mac = (PGmacaddr) value;
                } else if(value instanceof String) {
                    mac = new PGmacaddr((String) value);
                }
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert "
                        + value.getClass().getName() + " to "
                        + MACADDR.getName() + ": "
                        + ex.getMessage(), ex);
            }
        }
        
        return mac;
    }

    private PGinet convertToInet(Object value) {
        PGinet inet = null;
        if(value != null) {
            try {
                if(value instanceof IPTarget) {
                    inet = (IPTarget) value;
                } else if(value instanceof PGinet) {
                    inet = (PGinet) value;
                } else if(value instanceof PGcidr) {
                    inet = new PGinet(((PGcidr) value).getValue());
                } else if(value instanceof String) {
                    inet = new PGinet((String) value);
                } else if(value instanceof InetAddress) {
                    inet = new PGinet(((InetAddress) value).getHostAddress());
                }
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert "
                        + value.getClass().getName() + " to "
                        + INET.getName() + ": "
                        + ex.getMessage(), ex);
            }
        }
        
        return inet;
    }
    
    private PGcidr convertToCidr(Object value) {
        PGcidr cidr = null;
        if(value != null) {
            try {
                if(value instanceof IPNetwork) {
                    cidr = (IPNetwork) value;
                } else if(value instanceof PGcidr) {
                    cidr = (PGcidr) value;
                } else if(value instanceof PGinet) {
                    cidr = new PGcidr(((PGinet) value).getValue());
                } else if(value instanceof String) {
                    cidr = new PGcidr((String) value);
                } else if(value instanceof InetAddress) {
                    cidr = new PGcidr(((InetAddress) value).getHostAddress());
                }
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert "
                        + value.getClass().getName() + " to "
                        + CIDR.getName() + ": "
                        + ex.getMessage(), ex);
            }
        }
        
        return cidr;
    }
}
