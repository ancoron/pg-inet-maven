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

import org.ancoron.postgresql.jpa.cache.NetworkCache;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ancoron.postgresql.jpa.IPNetwork;
import org.ancoron.postgresql.jpa.cache.ThreadLocalNetworkCache;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.sessions.ArrayRecord;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGcidr;
import org.postgresql.net.PGinet;
import org.postgresql.util.PGobject;

/**
 * Supports mapping of <tt>org.postgresql.net.PGinet</tt> inside JPA entities
 * without having to reference any JDBC driver package explicitely.
 * 
 * <p>
 * <strong>Example usage:</strong>
 * 
 * <pre>
 * // ...
 * import org.ancoron.postgresql.jpa.IPNetwork;
 * import org.ancoron.postgresql.jpa.eclipselink.NetworkConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="networkConverter", converterClass=NetworkConverter.class)
 * public class NetworkEntity implements Serializable {
 *     
 *     // ...
 * 
 *     &#064;Convert("networkConverter")
 *     &#064;Column(name="c_network")
 *     private IPNetwork network;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * This implementation uses native PostgreSQL extensions to provide additional
 * data about a given network:
 * <ul>
 * <li><tt>family</tt> (extract family of address; 4 for IPv4, 6 for IPv6)</li>
 * <li><tt>broadcast</tt> (broadcast address for network)</li>
 * <li><tt>host</tt> (extract IP address as text)</li>
 * <li><tt>hostmask</tt> (construct host mask for network)</li>
 * <li><tt>masklen</tt> (extract netmask length)</li>
 * <li><tt>netmask</tt> (construct netmask for network)</li>
 * </ul>
 * This data is stored inside the {@link IPNetwork} instance.
 * </p>
 * 
 * <p>
 * <strong>Performance:</strong>
 * However, as this may be a performance penalty you can configure a cache for
 * networks and their data as well as deactivate the retrieval of extra data
 * altogether.
 * </p>
 * 
 * <p>
 * <strong>Caching:</strong>
 * A pure in-JVM cache is used by default to store extended data only once and
 * makes sure that the database doesn't have to be queried again for a known 
 * network.
 * </p>
 * 
 * <p>
 * <strong>Configuration:</strong>
 * However, as this may be a performance penalty you can configure a cache for
 * networks and their data as well as deactivate the retrieval of extra data
 * altogether.
 * </p>
 *
 * @author ancoron
 * 
 * @see IPNetwork
 * @see NetworkCache
 */
public class NetworkConverter implements Converter {

    private static final Logger log = Logger.getLogger(NetworkCache.class.getName());
    private static final String SELECT_EXTENDED_PREFIX = "SELECT "
            + "family(n.nw) as family, "
            + "host(broadcast(n.nw)) as broadcast, "
            + "host(n.nw) as host, "
            + "host(hostmask(n.nw)) as hostmask, "
            + "masklen(n.nw) as masklength, "
            + "host(netmask(n.nw)) as netmask "
            + "FROM (SELECT cidr '";
    private static final String SELECT_EXTENDED_POSTFIX = "' as nw) n";
    private static Class cc = null;
    private NetworkCache localCache = null;

    private NetworkCache getCache() {
        // reuse instance cache...
        if(localCache == null) {
            // reuse class configuration...
            if (cc == null) {
                String className = System.getProperty("org.ancoron.postgresql.jpa.networkCache",
                        ThreadLocalNetworkCache.class.getName());

                Class cacheClazz = null;
                try {
                    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    cacheClazz = tccl.loadClass(className);
                } catch (ClassNotFoundException ex) {
                    log.log(Level.SEVERE, "Unable to initialize NetworkCache - "
                            + "falling back to default", ex);
                    cacheClazz = ThreadLocalNetworkCache.class;
                }

                cc = cacheClazz;
            }

            NetworkCache nc = null;

            try {
                nc = (NetworkCache) cc.getDeclaredMethod("getInstance", new Class[] {})
                        .invoke(null, new Object[] {});
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Somehow the 'getInstance' Method is not "
                        + "available or failed for retrieving the current cache - "
                        + "falling back to explicit default", ex);
                nc = ThreadLocalNetworkCache.getInstance();
            }
            
            localCache = nc;
        }

        return localCache;
    }

    @Override
    public PGcidr convertObjectValueToDataValue(Object objectValue, Session session) {
        if (objectValue == null) {
            return null;
        } else if (objectValue instanceof IPNetwork) {
            return (IPNetwork) objectValue;
        } else if (objectValue instanceof PGcidr) {
            return (PGcidr) objectValue;
        } else if (objectValue instanceof String) {
            try {
                return new PGcidr((String) objectValue);
            } catch (SQLException ex) {
                throw new IllegalArgumentException("Unable to convert an object value", ex);
            }
        }

        throw new IllegalArgumentException("Unable to convert object value of type "
                + objectValue.getClass().getName() + " into a "
                + PGinet.class.getName());
    }

    @Override
    public IPNetwork convertDataValueToObjectValue(Object dataValue, Session session) {
        IPNetwork net = null;
        if (dataValue == null) {
            return net;
        } else if (dataValue instanceof PGcidr) {
            net = new IPNetwork((PGcidr) dataValue);
        } else if (dataValue instanceof PGobject) {
            // this is a fallback in case special JDBC types are not available...
            net = new IPNetwork(((PGobject) dataValue).getValue());
        }

        if (net == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + IPNetwork.class.getName());
        }

        final Object[] cached = getCache().get(net.getValue());

        if (cached[0] == null) {
            // only query for unknown networks...
            Vector res = session.executeSQL(
                    SELECT_EXTENDED_PREFIX
                    + net.getValue()
                    + SELECT_EXTENDED_POSTFIX);

            // System.out.println("IPNetwork initialize result: " + res);

            ArrayRecord rec = (ArrayRecord) res.get(0);
            boolean ipv6 = rec.get("family").equals("6");
            net.setV6(ipv6);
            cached[0] = Boolean.valueOf(ipv6);

            // all IP addresses returned are literal IPs...
            try {
                InetAddress bc = InetAddress.getByName((String) rec.get("broadcast"));
                byte[] broadcast = bc.getAddress();
                net.setBroadcastAddress(broadcast);
                cached[1] = broadcast;

                byte[] hostmask = InetAddress.getByName((String) rec.get("hostmask")).getAddress();
                net.setHostmaskAddress(hostmask);
                cached[2] = hostmask;

                short maskLength = ((Integer) rec.get("masklength")).shortValue();
                net.setMaskLength(maskLength);
                cached[3] = Short.valueOf(maskLength);

                byte[] netmask = InetAddress.getByName((String) rec.get("netmask")).getAddress();
                net.setNetmaskAddress(netmask);
                cached[4] = netmask;

                getCache().put(net.getValue(), cached);
            } catch (UnknownHostException x) {
                throw new IllegalArgumentException("Unable to fill extended values for the "
                        + IPNetwork.class.getName() + " using value " + net.getValue(), x);
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
    }
}
