/*
 * Copyright 2011 ancoron.
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
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.ancoron.postgresql.jpa.Network;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.ArrayRecord;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.queries.DataReadQuery;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGinet;
import org.postgresql.util.PGobject;

/**
 * Supports mapping of <tt>org.postgresql.net.PGinet</tt> inside JPA entities.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * @Entity
 * @Converter(name="pginetConverter", converterClass=PGinetConverter.class)
 * public class PGinetEntity implements Serializable {
 *     
 * 
 *     @Convert("pginetConverter")
 *     @Column(name="c_network", nullable=false, insertable=true, updatable=false)
 *     private PGinet network;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author ancoron
 */
public class NetworkConverter implements Converter {

    private static final String INIT_NETWORK_QUERY = "org.ancoron.postgresql.jpa.Network.initialize";
    private static final String SELECT_EXTENDED_PREFIX = "SELECT "
            + "family(n.nw) as family, "
            + "host(broadcast(n.nw)) as broadcast, "
            + "host(n.nw) as host, "
            + "host(hostmask(n.nw)) as hostmask, "
            + "masklen(n.nw) as maskLength, "
            + "host(netmask(n.nw)) as netmask "
            + "FROM (SELECT inet '";
    private static final String SELECT_EXTENDED_POSTFIX = "' as nw) n";
    
    private static final ThreadLocal<ExtendedPropertiesCache> cache = new InheritableThreadLocal<ExtendedPropertiesCache>();
    
    private ExtendedPropertiesCache getCache() {
        if(cache.get() == null) {
            cache.set(new ExtendedPropertiesCache() {
                
                private Map<String, Object[]> internal = new HashMap<String, Object[]>(100);

                @Override
                public Object[] get(PGinet net) {
                    Object[] res = internal.get(net.getValue());
                    
                    if(res == null) {
                        res = new Object[6];
                    }
                    
                    return res;
                }

                @Override
                public void put(PGinet net, Object[] properties) {
                    internal.put(net.getValue(), properties);
                }
            });
        }
        
        return cache.get();
    }

    @Override
    public PGinet convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return null;
        } else if (objectValue instanceof Network) {
            return ((Network) objectValue).getNet();
        } else if (objectValue instanceof PGinet) {
            return (PGinet) objectValue;
        } else if (objectValue instanceof String) {
            try {
                return new PGinet((String) objectValue);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert object value of type "
                + objectValue.getClass().getName() + " into a "
                + PGinet.class.getName());
    }

    @Override
    public Network convertDataValueToObjectValue(Object dataValue, Session session) {
        Network net = null;
        if (dataValue == null) {
            return net;
        } else if (dataValue instanceof PGinet) {
            net = new Network((PGinet) dataValue);
        } else if (dataValue instanceof PGobject) {
            net = new Network(((PGobject) dataValue).getValue());
        }
        
        if(net == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + Network.class.getName());
        }
        
        final Object[] cached = getCache().get(net.getNet());
        
        if(cached[0] == null) {
            // only query for unknown networks...
            Vector res = session.executeSQL(
                    SELECT_EXTENDED_PREFIX
                    + net.getNet().getValue()
                    + SELECT_EXTENDED_POSTFIX);

            // System.out.println("Network initialize result: " + res);

            ArrayRecord rec = (ArrayRecord) res.get(0);
            boolean ipv6 = rec.get("family").equals("6");
            net.setV6(ipv6);
            cached[0] = Boolean.valueOf(ipv6);

            // all IP addresses returned are literal IPs...
            try {
                byte[] broadcast = InetAddress.getByName((String) rec.get("broadcast")).getAddress();
                net.setBroadcast(broadcast);
                cached[1] = broadcast;

                InetAddress host = InetAddress.getByName((String) rec.get("host"));
                net.setHost(host);
                cached[2] = host;

                byte[] hostmask = InetAddress.getByName((String) rec.get("hostmask")).getAddress();
                net.setHostmask(hostmask);
                cached[3] = hostmask;

                short maskLength = ((Integer) rec.get("maskLength")).shortValue();
                net.setMaskLength(maskLength);
                cached[4] = Short.valueOf(maskLength);

                byte[] netmask = InetAddress.getByName((String) rec.get("netmask")).getAddress();
                net.setNetmask(netmask);
                cached[5] = netmask;
                
                getCache().put(net.getNet(), cached);
            } catch(UnknownHostException x) {
                throw new IllegalArgumentException("Unable to fill extended values for the "
                        + Network.class.getName() + " using value " + net.getNet().getValue(), x);
            }
            
            res = null;
        }

        return net;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public void initialize(DatabaseMapping mapping, Session session) {
        final DatabaseField field = mapping.getField();
        field.setSqlType(java.sql.Types.OTHER);
        field.setTypeName("inet");
        field.setColumnDefinition("INET");
        
        String sql = "SELECT broadcast(n) FROM (SELECT inet ?1)";
        
        DataReadQuery read = new DataReadQuery(sql);
        List<Class> argTypes = new ArrayList<Class>();
        argTypes.add(String.class);
        read.setArgumentTypes(argTypes);
        
        session.addQuery(INIT_NETWORK_QUERY, read);
    }
}
