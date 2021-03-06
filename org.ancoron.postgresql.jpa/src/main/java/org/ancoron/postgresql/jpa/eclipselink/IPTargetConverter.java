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

import org.ancoron.postgresql.jpa.IPTarget;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.net.PGinet;

/**
 * Supports mapping of {@link IPTarget} inside JPA entities
 * without having to reference any JDBC driver package explicitely.
 * 
 * <p>
 * <strong>Example usage:</strong>
 * 
 * <pre>
 * // ...
 * import org.ancoron.postgresql.jpa.IPTarget;
 * import org.ancoron.postgresql.jpa.eclipselink.IPTargetConverter;
 * 
 * // ...
 * 
 * &#064;Entity
 * &#064;Converter(name="ipConverter", converterClass=IPTargetConverter.class)
 * public class IPEntity implements Serializable {
 *     
 *     // ...
 * 
 *     &#064;Convert("ipConverter")
 *     &#064;Column(name="c_ip")
 *     private IPTarget ip;
 * 
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author ancoron
 * 
 * @see IPTarget
 */
public class IPTargetConverter extends PGinetConverter implements Converter {

    @Override
    public PGinet convertObjectValueToDataValue(Object objectValue, Session session) {
        if(objectValue == null) {
            return null;
        } else if (objectValue instanceof IPTarget) {
            return (IPTarget) objectValue;
        }

        return super.convertObjectValueToDataValue(objectValue, session);
    }

    @Override
    public IPTarget convertDataValueToObjectValue(Object dataValue, Session session) {
        IPTarget net = null;
        if (dataValue == null) {
            return net;
        } else {
            PGinet inet = super.convertDataValueToObjectValue(dataValue, session);
            if(inet != null) {
                net = new IPTarget(inet);
            }
        }

        if (net == null) {
            throw new IllegalArgumentException("Unable to convert data value of type "
                    + dataValue.getClass().getName() + " into a "
                    + IPTarget.class.getName());
        }

        return net;
    }
}
