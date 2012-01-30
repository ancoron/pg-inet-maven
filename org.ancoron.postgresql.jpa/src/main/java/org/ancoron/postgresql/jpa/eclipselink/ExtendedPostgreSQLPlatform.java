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

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
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

    @Override
    public int getJDBCType(Class javaType) {
        if (javaType == INET) {
            return TYPE_INET;
        } else if(javaType == CIDR) {
            return TYPE_CIDR;
        } else if(javaType == MACADDR) {
            return TYPE_MAC;
        } else if(javaType == IPNetwork.class) {
            return TYPE_NET;
        }
        return super.getJDBCType(javaType);
    }

    @Override
    public int getJDBCType(DatabaseField field) {
        if(IPNetwork.class.isAssignableFrom(field.getType())) {
            return TYPE_NET;
        }
        return super.getJDBCType(field);
    }

    @Override
    public String getJdbcTypeName(int jdbcType) {
        return super.getJdbcTypeName(jdbcType);
    }

    @Override
    protected Hashtable buildFieldTypes() {
        Hashtable fieldTypeMapping = super.buildFieldTypes();
        
        log.info("Generating FieldTypeMapping for PostgreSQL specific network data types");

        fieldTypeMapping.put(IPNetwork.class, new FieldTypeDefinition("INET", false));
        fieldTypeMapping.put(INET, new FieldTypeDefinition("INET", false));
        fieldTypeMapping.put(CIDR, new FieldTypeDefinition("CIDR", false));
        fieldTypeMapping.put(MACADDR, new FieldTypeDefinition("MACADDR", false));

        return fieldTypeMapping;
    }

    
    
    @Override
    protected Map<String, Class> buildClassTypes() {
        Map<String, Class> classTypeMapping = super.buildClassTypes();

        log.info("Generating ClassTypes for PostgreSQL specific network data types");

        // Key the Map the other way for table creation.
        classTypeMapping.put("INET", IPNetwork.class);
        classTypeMapping.put("CIDR", IPNetwork.class);
        classTypeMapping.put("MACADDR", MACADDR);

        return classTypeMapping;
    }

    @Override
    public ConnectionCustomizer createConnectionCustomizer(Accessor accessor, AbstractSession session) {
        return new PostgreSQLConnectionCustomizer(accessor, session);
    }
    
    @Override
    public boolean shouldUseCustomModifyForCall(DatabaseField field) {
        Class type = field.getType();
        if ((type != null) && (PGobject.class.isAssignableFrom(type)
                || type == IPNetwork.class || type == IPTarget.class))
        {
            return true;
        }
        return super.shouldUseCustomModifyForCall(field);
    }

    @Override
    public Object getCustomModifyValueForCall(Call call, Object value, DatabaseField field, boolean shouldBind) {
        Class type = field.getType();
        if ((type != null) && PGobject.class.isAssignableFrom(type)) {
            if(value == null) {
                return null;
            }

            if(IPNetwork.class.isAssignableFrom(type)) {
                value = convertToCidr((IPNetwork) value);
            } else if (IPTarget.class.isAssignableFrom(type)) {
                value = convertToInet((IPTarget) value);
            } else if (INET.equals(type)) {
                value = convertToInet(value);
            } else if (CIDR.equals(type)) {
                value = convertToCidr(value);
            } else if (MACADDR.equals(type)) {
                value = convertToMac(value);
            }

            return new BindCallCustomParameter(value);
        }

        return super.getCustomModifyValueForCall(call, value, field, shouldBind);
    }

    @Override
    public Object convertToDatabaseType(Object value) {
        if(value == null) {
            return null;
        }
        
        if(value != null) {
            if((value instanceof IPTarget) || (value instanceof PGinet)) {
                return convertToInet(value);
            } else if((value instanceof IPNetwork) || (value instanceof PGcidr)) {
                return convertToCidr(value);
            } else if((value instanceof PGmacaddr)) {
                return convertToMac(value);
            }
        }
        
        return super.convertToDatabaseType(value);
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
    
    protected Vector buildToInetVec() {
        Vector vec = new Vector();
        vec.addElement(IPTarget.class);
        vec.addElement(String.class);
        vec.addElement(InetAddress.class);
        vec.addElement(INET);
        return vec;
    }

    protected Vector buildToCidrVec() {
        Vector vec = new Vector();
        vec.addElement(IPNetwork.class);
        vec.addElement(String.class);
        vec.addElement(CIDR);
        return vec;
    }

    protected Vector buildToMacaddrVec() {
        Vector vec = new Vector();
        vec.addElement(String.class);
        vec.addElement(MACADDR);
        return vec;
    }

    @Override
    public Vector getDataTypesConvertedFrom(Class javaClass) {
        if (dataTypesConvertedFromAClass == null) {
            dataTypesConvertedFromAClass = new Hashtable(5);
        }

        Vector dataTypes = (Vector) dataTypesConvertedFromAClass.get(javaClass);
        if (dataTypes != null) {
            return dataTypes;

        }

        dataTypes = super.getDataTypesConvertedFrom(javaClass);
        if ((javaClass == IPTarget.class) || (javaClass == InetAddress.class)) {
            dataTypes.addElement(INET);
        } else if ((javaClass == IPNetwork.class)) {
            dataTypes.addElement(CIDR);
        }

        dataTypesConvertedFromAClass.put(javaClass, dataTypes);
        return dataTypes;
    }

    @Override
    public Vector getDataTypesConvertedTo(Class javaClass) {
        if (dataTypesConvertedToAClass == null) {
            dataTypesConvertedToAClass = new Hashtable(5);
        }

        Vector dataTypes = (Vector) dataTypesConvertedToAClass.get(javaClass);
        if (dataTypes != null) {
            return dataTypes;
        }

        if(javaClass == CIDR || IPNetwork.class.isAssignableFrom(javaClass)) {
            dataTypes = buildToCidrVec();
        } else if (javaClass == INET || IPTarget.class.isAssignableFrom(javaClass)) {
            dataTypes = buildToInetVec();
        } else if (javaClass == MACADDR) {
            dataTypes = buildToMacaddrVec();
        } else {
            dataTypes = super.getDataTypesConvertedTo(javaClass);
        }

        dataTypesConvertedToAClass.put(javaClass, dataTypes);
        
        return dataTypes;
    }
}
