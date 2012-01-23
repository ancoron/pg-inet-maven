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

import org.ancoron.postgresql.jpa.IPNetwork;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGcidr;

/**
 * Supports mapping of {@link IPNetwork} inside JPA entities
 * without having to reference any JDBC driver package explicitely.
 * 
 * <p>
 * <strong>Example usage:</strong>
 * 
 * <pre>
 * // ...
 * import org.ancoron.postgresql.jpa.IPNetwork;
 * import org.ancoron.postgresql.jpa.eclipselink.IPNetworkConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="networkConverter", converterClass=IPNetworkConverter.class)
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
 * @author ancoron
 * 
 * @see IPNetwork
 */
public class IPNetworkConverter extends PGcidrConverter implements Converter {

    @Override
    public PGcidr convertObjectValueToDataValue(Object objectValue, Session session) {
        if(objectValue == null) {
            return null;
        } else if(objectValue instanceof IPNetwork) {
            return (IPNetwork) objectValue;
        }

        return super.convertObjectValueToDataValue(objectValue, session);
    }

    @Override
    public IPNetwork convertDataValueToObjectValue(Object dataValue, Session session) {
        IPNetwork net = null;
        if (dataValue == null) {
            return net;
        } else {
            PGcidr cidr = super.convertDataValueToObjectValue(dataValue, session);
            if(cidr != null) {
                net = new IPNetwork(cidr);
            }
        }

        if (net == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + IPNetwork.class.getName());
        }

        return net;
    }
}
